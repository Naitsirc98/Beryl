#version 450 core

#extension GL_KHR_vulkan_glsl: require
#extension GL_ARB_bindless_texture: require

@include "structs/lights.glsl"

#define MAX_POINT_LIGHTS 10
#define MAX_SPOT_LIGHTS 10

struct Material {

    vec4 ambientColor;
    vec4 diffuseColor;
    vec4 specularColor;
    vec4 emissiveColor;
    sampler2D ambientMap;
    sampler2D diffuseMap;
    sampler2D specularMap;
    sampler2D emissiveMap;
    float shininess;
};


layout(std140, set = 0, binding = 0) uniform Camera {
    mat4 u_ProjectionViewMatrix;
    vec4 u_CameraPosition;
};

layout(std430, bindless_sampler, binding = 3) buffer Materials {
    Material u_Material[];
};

layout(std140, set = 2, binding = 1) uniform Lights {
    Light u_DirectionalLight;
    Light u_PointLights[MAX_POINT_LIGHTS];
    Light u_SpotLights[MAX_SPOT_LIGHTS];
    vec4 u_AmbientColor;
    int u_PointLightsCount;
    int u_SpotLightsCount;
};


layout(location = 0) in VertexData {
    vec3 position;
    vec3 normal;
    vec2 texCoords;
    flat uint materialIndex;
} vertexData;


layout(location = 0) out vec4 out_FinalColor;


vec3 cameraDirection;

vec4 materialAmbientColor;
vec4 materialDiffuseColor;
vec4 materialSpecularColor;
vec4 materialEmissiveColor;

vec4 computeLighting();
vec4 computeDirectionalLighting(Light light);
vec4 computePointLighting(Light light);
vec4 computeSpotLighting(Light light);


void main() {

    cameraDirection = normalize(u_CameraPosition.xyz - vertexData.position);

    Material material = u_Material[vertexData.materialIndex];

    materialAmbientColor = material.ambientColor * texture(material.ambientMap, vertexData.texCoords);
    materialDiffuseColor = material.diffuseColor * texture(material.diffuseMap, vertexData.texCoords);
    materialSpecularColor = material.specularColor * texture(material.specularMap, vertexData.texCoords);
    materialEmissiveColor = material.emissiveColor * texture(material.emissiveMap, vertexData.texCoords);

    if(materialAmbientColor.a + materialDiffuseColor.a < 0.001) {
        discard;
    }

    // TODO: check if material is affected by light
    out_FinalColor = computeLighting() + materialEmissiveColor;
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

    vec4 color = ambientColor + diffuseSpecularColor;

    return color;
}

float computeAngle(vec3 v1, vec3 v2) {
    return max(dot(v1, v2), 0.0f);
}

float computeAttenuation(vec3 lightPosition, float constant, float linear, float quadratic) {

    float distance = length(lightPosition - vertexData.position);

    return 1.0f /
        (constant + linear * distance + quadratic * (distance * distance));
}

float computeIntensity(vec3 normalizedDirection, vec3 lightDirection, float cutOff, float outerCutOff) {

    float theta = dot(normalizedDirection, normalize(lightDirection));

    float epsilon = (cutOff - outerCutOff);

    return clamp((theta - outerCutOff) / epsilon, 0.0f, 1.0f);
}

vec4 computeDiffuseColor(vec4 lightColor, vec3 lightDirection) {

	float diffuse = computeAngle(vertexData.normal, lightDirection);

	return vec4(vec3(lightColor * (diffuse * materialDiffuseColor)), materialDiffuseColor.a);
}

vec4 computeSpecularColor(vec4 lightColor, vec3 lightDirection) {

    // Phong
	// vec3 reflectDirection = reflect(-lightDirection, vertexData.normal);

    // float specular = pow(computeAngle(cameraDirection, reflectDirection), u_Material.shininess);

    // Blinn-Phong
    vec3 viewDirection = normalize(u_CameraPosition.xyz - vertexData.position);

    vec3 reflectDirection = reflect(-lightDirection, vertexData.normal);

    vec3 halfwayDirection = normalize(lightDirection + viewDirection);

    float specular = pow(computeAngle(vertexData.normal, halfwayDirection), u_Material.shininess);

	return vec4(vec3(lightColor * (specular * materialSpecularColor)), materialSpecularColor.a);
}


vec4 computeDirectionalLighting(Light light) {

    vec3 direction = normalize(-light.direction.xyz);

    return computeDiffuseColor(light.color, direction)
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
