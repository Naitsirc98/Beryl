#version 450 core

#extension GL_ARB_bindless_texture: require

#define MAX_POINT_LIGHTS 10
#define MAX_SPOT_LIGHTS 10

@include "structs/lights.glsl"
@include "structs/phong_material.glsl"
@include "structs/fog.glsl"
@include "structs/shadow_cascade.glsl"

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
    ShadowCascade u_ShadowCascades[MAX_SHADOW_CASCADES_COUNT];
    bool u_ShadowsEnabled; 
};

layout(location = 0) in FragmentData {
    vec3 position;
    vec3 normal;
    vec2 texCoords;
    flat int materialIndex;
    vec4 positionDirLightSpace[MAX_SHADOW_CASCADES_COUNT];
} fragment;


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

vec4 applyFogEffect(vec4 fragmentColor);

void main() {

    cameraDirection = normalize(u_Camera.position.xyz - fragment.position);

    material = u_Materials[fragment.materialIndex];

    if(testMaterialFlag(material.flags, NORMAL_MAP_PRESENT)) {
        fragmentNormal = texture(material.normalMap, fragment.texCoords).rgb;
        fragmentNormal = vec3(fragmentNormal.r, fragmentNormal.g, fragmentNormal.b) * 2.0 - 1.0;
        fragmentNormal = normalize(fragmentNormal);
    } else {
        fragmentNormal = normalize(fragment.normal);
    }

    // float lod = distance(u_Camera.position.xyz, fragment.position) / 100.0;

    vec2 texCoords = fragment.texCoords * material.tiling;

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

    fragmentColor.a *= material.alpha;

    out_FragmentColor = fragmentColor;
}


vec4 applyFogEffect(vec4 fragmentColor) {

    vec3 fogColor = u_Fog.color.rgb * u_AmbientColor.rgb;

    if(u_DirectionalLight.type != NULL) {
        fogColor *= u_DirectionalLight.color.rgb;
    }

    float distance = length(u_Camera.position.xyz - fragment.position) / 100.0;

    float exponent = distance * u_Fog.density;

    if(exponent == 0.0) {
        return fragmentColor;
    }

    float fogFactor = 1.0 / exp(exponent * exponent);
    fogFactor = clamp(fogFactor, 0.0, 1.0);

    vec3 finalColor = mix(fogColor, fragmentColor.rgb, fogFactor);

    return vec4(finalColor, fragmentColor.a);
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

    float shadows = 0.0;

    if(u_ShadowsEnabled) {
        shadows = computeDirShadows(u_Camera.projectionViewMatrix, fragment.position, fragment.positionDirLightSpace, u_ShadowCascades);
    }

    vec4 color = ambientColor + diffuseSpecularColor * (1.0 - shadows);

    return color;
}

float computeAngle(vec3 v1, vec3 v2) {
    return max(dot(v1, v2), 0.0);
}

float computeAttenuation(vec3 lightPosition, float constant, float linear, float quadratic) {

    float distance = length(lightPosition - fragment.position);

    return 1.0 /
        (constant + linear * distance + quadratic * (distance * distance));
}

float computeIntensity(vec3 lightToFragment, vec3 lightDirection, float cutOff, float outerCutOff) {

    float theta = dot(lightToFragment, normalize(-lightDirection));

    float epsilon = (cutOff - outerCutOff);

    return clamp((theta - outerCutOff) / epsilon, 0.0, 1.0);
}

vec4 computeDiffuseColor(vec4 lightColor, vec3 lightDirection) {

	float diffuse = computeAngle(fragmentNormal, lightDirection);

	return vec4(lightColor.rgb * (diffuse * materialDiffuseColor.rgb), materialDiffuseColor.a);
}

vec4 computeSpecularColor(vec4 lightColor, vec3 lightDirection) {

    // Phong
	// vec3 reflectDirection = reflect(-lightDirection, fragmentNormal);

    // float specular = pow(computeAngle(cameraDirection, reflectDirection), material.shininess);

    // Blinn-Phong
    vec3 halfwayDirection = normalize(lightDirection + cameraDirection);

    float specular = pow(computeAngle(fragmentNormal, halfwayDirection), material.shininess);

	return vec4(lightColor.rgb * (specular * materialSpecularColor.rgb), materialSpecularColor.a);
}


vec4 computeDirectionalLighting(Light light) {

    vec3 direction = normalize(-light.direction.xyz);

    vec4 diffuseColor = computeDiffuseColor(light.color, direction);

    vec4 specularColor = computeSpecularColor(light.color, direction);

    return diffuseColor + specularColor;
}


vec4 computePointLighting(Light light) {

    vec3 direction = normalize(light.position.xyz - fragment.position);

    float attenuation = computeAttenuation(light.position.xyz, light.constant, light.linear, light.quadratic);

    vec4 diffuseColor = computeDiffuseColor(light.color, direction) * attenuation;

    vec4 specularColor = computeSpecularColor(light.color, direction) * attenuation;

    return diffuseColor + specularColor;
}

vec4 computeSpotLighting(Light light) {

    vec3 lightToFragment = normalize(light.position.xyz - fragment.position);

    float attenuation = computeAttenuation(light.position.xyz, light.constant, light.linear, light.quadratic);

    float intensity = computeIntensity(lightToFragment, light.direction.xyz, light.cutOff, light.outerCutOff);

    vec4 diffuseColor = computeDiffuseColor(light.color, lightToFragment) * intensity * attenuation;

    vec4 specularColor = computeSpecularColor(light.color, lightToFragment) * intensity * attenuation;

    return diffuseColor + specularColor;
}
