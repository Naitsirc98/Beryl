#version 450 core

#extension GL_KHR_vulkan_glsl: require
#extension GL_ARB_bindless_texture: require


@include "structs/lights.glsl"
@include "structs/material.glsl"
@include "structs/fog.glsl"

#define MAX_SHADOW_CASCADES_COUNT 3
#define MAX_POINT_LIGHTS 10
#define MAX_SPOT_LIGHTS 10

layout(std140, binding = 0) uniform Camera {
    mat4 projectionViewMatrix;
    vec4 position;
} u_Camera;


layout(std140, binding = 1) uniform Lights {
    Light u_DirectionalLight;
    Light u_PointLights[MAX_POINT_LIGHTS];
    Light u_SpotLights[MAX_SPOT_LIGHTS];
    vec4 u_AmbientColor;
    Fog u_Fog;
    int u_PointLightsCount;
    int u_SpotLightsCount;
};

layout(std430, binding = 3) readonly buffer Materials {
    PhongMaterial u_Materials[];
};

layout(std140, binding = 5) uniform ShadowsInfo {
    mat4 u_DirLightMatrices[MAX_SHADOW_CASCADES_COUNT];
    float u_CascadeFarPlanes[MAX_SHADOW_CASCADES_COUNT]; 
};

uniform sampler2D u_DirShadowMaps[MAX_SHADOW_CASCADES_COUNT];

layout(location = 0) in VertexData {
    vec3 position;
    vec3 normal;
    vec2 texCoords;
    flat int materialIndex;
    vec4 positionDirLightSpace[MAX_SHADOW_CASCADES_COUNT];
} vertexData;


layout(location = 0) out vec4 out_FragmentColor;


vec3 cameraDirection;
vec3 fragmentNormal;

PhongMaterial material;

vec4 materialAmbientColor;
vec4 materialDiffuseColor;
vec4 materialSpecularColor;
vec4 materialEmissiveColor;

vec4 computeLighting();
vec4 computeDirectionalLighting(Light light);
vec4 computePointLighting(Light light);
vec4 computeSpotLighting(Light light);

float computeDirShadows();

vec4 applyFogEffect(vec4 fragmentColor);

void main() {

    cameraDirection = normalize(u_Camera.position.xyz - vertexData.position);

    material = u_Materials[vertexData.materialIndex];

    if(testMaterialFlag(material.flags, NORMAL_MAP_PRESENT)) {
        fragmentNormal = texture(material.normalMap, vertexData.texCoords).rgb;
        fragmentNormal = vec3(fragmentNormal.r, fragmentNormal.g, fragmentNormal.b) * 2.0 - 1.0;
        fragmentNormal = normalize(fragmentNormal);
    } else {
        fragmentNormal = normalize(vertexData.normal);
    }

    // float lod = distance(u_Camera.position.xyz, vertexData.position) / 100.0;

    vec2 texCoords = vertexData.texCoords * material.texCoordsFactor;

    materialAmbientColor = material.ambientColor * texture(material.ambientMap, texCoords);
    materialDiffuseColor = material.diffuseColor * texture(material.diffuseMap, texCoords);
    materialSpecularColor = material.specularColor * texture(material.specularMap, texCoords);
    materialEmissiveColor = material.emissiveColor * texture(material.emissiveMap, texCoords);

    // IMPORTANT!!! IF NO MATERIAL IS USED, THIS WILL DISCARD ANY OUTPUT!! COULD CAUSE PROBLEMS WHEN DEBUGGING!!!!!
    if(material.alpha == 0.0 || materialAmbientColor.a + materialDiffuseColor.a == 0.0) {
        discard;
    }

    vec4 fragmentColor = vec4(0.0);

    if(materialEmissiveColor.rgb != vec3(0.0)) {
        fragmentColor = materialEmissiveColor;
    } else {
        fragmentColor = computeLighting();
    }

    if(u_Fog.color.a != 0.0) {
        fragmentColor = applyFogEffect(fragmentColor);
    }



    int depthMapIndex;

    float fragmentDepth = distance(u_Camera.position.xyz, vertexData.position);

    // Select the correct cascade shadow map for this fragment
    for(int i = 0; i < MAX_SHADOW_CASCADES_COUNT; i++) {
        if(fragmentDepth < u_CascadeFarPlanes[i]) {
            depthMapIndex = i;
            break;
        }
    }

    vec4 color = vec4(0, 0, 0, 1);

    color[depthMapIndex] = 1;

  
    out_FragmentColor = fragmentColor;
}


vec4 applyFogEffect(vec4 fragmentColor) {

    vec3 fogColor = u_Fog.color.rgb * (u_AmbientColor.rgb * u_DirectionalLight.color.rgb);

    float distance = length(u_Camera.position.xyz - vertexData.position) / 100.0;

    float exponent = distance * u_Fog.density;

    if(exponent == 0.0) {
        return fragmentColor;
    }

    float fogFactor = 1.0 / exp(exponent * exponent);
    fogFactor = clamp(fogFactor, 0.0, 1.0);

    vec3 finalColor = mix(fogColor, fragmentColor.rgb, fogFactor);

    return vec4(finalColor, fragmentColor.a);
}

float computeDirShadows() {

    int depthMapIndex = 0;

    float fragmentDepth = (u_Camera.projectionViewMatrix * vec4(vertexData.position, 1.0)).z;// vertexData.position.z;//distance(u_Camera.position.xyz, vertexData.position);

    // Select the correct cascade shadow map for this fragment
    for(int i = 0; i < MAX_SHADOW_CASCADES_COUNT; i++) {
        if(fragmentDepth < u_CascadeFarPlanes[i]) {
            depthMapIndex = i;
            break;
        }
    }

    // Transform from screen coordinates to texture coordinates
    vec4 positionDirLightSpace = vertexData.positionDirLightSpace[depthMapIndex];

    vec3 projCoords = positionDirLightSpace.xyz;// / positionDirLightSpace.w;

    projCoords = projCoords * 0.5 + 0.5;

    if(projCoords.z > 1.0) {
        return 0.0;
    }

    float bias = 0.005;//max(0.05 * (1.0 - dot(fragmentNormal, u_DirectionalLight.direction.xyz)), 0.005);

    float shadow = 0.0;

    float numberOfSamples = 9.0;

    sampler2D depthMap = u_DirShadowMaps[depthMapIndex];

    vec2 inc = 1.0 / textureSize(depthMap, 0);

    for(int row = -1; row <= 1; ++row) {

        for(int col = -1; col <= 1; ++col) {

            vec2 shadowSampleCoords = projCoords.xy + vec2(row, col) * inc;

            float textDepth = texture(depthMap, shadowSampleCoords).r;

            shadow += projCoords.z - bias > textDepth ? 1.0 : 0.0;
        }
    }

    shadow /= numberOfSamples;

    return shadow;
}

vec4 computeLighting() {

    vec4 ambientColor = u_AmbientColor * materialAmbientColor;

    vec4 diffuseSpecularColor = vec4(0.0);

    if(u_DirectionalLight.type != NULL) {
        diffuseSpecularColor += computeDirectionalLighting(u_DirectionalLight);
    }

    for(int i = 0;i < u_PointLightsCount;++i) {
        diffuseSpecularColor += computePointLighting(u_PointLights[i]);
    }

    for(int i = 0;i < u_SpotLightsCount;++i) {
        diffuseSpecularColor += computeSpotLighting(u_SpotLights[i]);
    }

    float shadows = computeDirShadows();

    vec4 color = ambientColor + diffuseSpecularColor * (1.0 - shadows);

    return color;
}

float computeAngle(vec3 v1, vec3 v2) {
    return max(dot(v1, v2), 0.0);
}

float computeAttenuation(vec3 lightPosition, float constant, float linear, float quadratic) {

    float distance = length(lightPosition - vertexData.position);

    return 1.0 /
        (constant + linear * distance + quadratic * (distance * distance));
}

float computeIntensity(vec3 normalizedDirection, vec3 lightDirection, float cutOff, float outerCutOff) {

    float theta = dot(normalizedDirection, normalize(lightDirection));

    float epsilon = (cutOff - outerCutOff);

    return clamp((theta - outerCutOff) / epsilon, 0.0, 1.0);
}

vec4 computeDiffuseColor(vec4 lightColor, vec3 lightDirection) {

	float diffuse = computeAngle(fragmentNormal, lightDirection);

	return vec4(vec3(lightColor * (diffuse * materialDiffuseColor)), materialDiffuseColor.a);
}

vec4 computeSpecularColor(vec4 lightColor, vec3 lightDirection) {

    // Phong
	// vec3 reflectDirection = reflect(-lightDirection, fragmentNormal);

    // float specular = pow(computeAngle(cameraDirection, reflectDirection), u_Material.shininess);

    // Blinn-Phong
    vec3 reflectDirection = reflect(-lightDirection, fragmentNormal);

    vec3 halfwayDirection = normalize(lightDirection + cameraDirection);

    float specular = pow(computeAngle(fragmentNormal, halfwayDirection), material.shininess);

	return vec4(vec3(lightColor * (specular * materialSpecularColor)), materialSpecularColor.a);
}


vec4 computeDirectionalLighting(Light light) {

    vec3 direction = normalize(-light.direction.xyz);

    return computeDiffuseColor(light.color, direction);
         + computeSpecularColor(light.color, direction);
}


vec4 computePointLighting(Light light) {

    vec3 direction = normalize(light.position.xyz - vertexData.position);

    float attenuation = computeAttenuation(light.position.xyz, light.constant, light.linear, light.quadratic);

    return (computeDiffuseColor(light.color, direction)
         + computeSpecularColor(light.color, direction)) * attenuation;
}

vec4 computeSpotLighting(Light light) {

    vec3 direction = normalize(light.position.xyz - vertexData.position);

    float attenuation = computeAttenuation(light.position.xyz, light.constant, light.linear, light.quadratic);

    float intensity = computeIntensity(direction, light.direction.xyz, light.cutOff, light.outerCutOff);

    return (computeDiffuseColor(light.color * intensity, direction)
         + computeSpecularColor(light.color * intensity, direction)) * attenuation;
}
