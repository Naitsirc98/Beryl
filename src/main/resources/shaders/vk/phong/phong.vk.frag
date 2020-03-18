#version 450 core

@include "structs/phong_material.glsl"
@include "structs/lights.glsl"

#define MAX_LIGHTS_COUNT 64
#define LIGHT_BUFFER_SIZE (SPOT_LIGHT_SIZE + 1) * MAX_LIGHTS_COUNT + 1

layout(push_constant) uniform PushConstant {
    mat4 u_MVP;
    vec4 u_CameraPosition;
};

layout(binding = 1) uniform MaterialUniformBuffer {
    PhongMaterial u_Material;
};

layout(binding = 2) uniform sampler2D u_AmbientMap;
layout(binding = 3) uniform sampler2D u_DiffuseMap;
layout(binding = 4) uniform sampler2D u_SpecularMap;
layout(binding = 5) uniform sampler2D u_EmissiveMap;

layout(binding = 6) uniform LightsUniformBuffer {
    vec4 u_LightBuffer[256];
};

layout(location = 0) in VertexData {
    vec3 position;
    vec3 normal;
    vec2 textureCoords;
} vertexData;

layout(location = 0) out vec4 out_FinalColor;

vec3 cameraDirection = normalize(u_CameraPosition.xyz - vertexData.position);

vec4 materialAmbientColor = u_Material.ambientColor * texture(u_AmbientMap, vertexData.textureCoords);
vec4 materialDiffuseColor = u_Material.diffuseColor * texture(u_DiffuseMap, vertexData.textureCoords);
vec4 materialSpecularColor = u_Material.specularColor * texture(u_SpecularMap, vertexData.textureCoords);
vec4 materialEmissiveColor = u_Material.emissiveColor * texture(u_EmissiveMap, vertexData.textureCoords);

vec4 computeLighting();
vec4 computeDirectionalLighting(int offset);
vec4 computePointLighting(int offset);
vec4 computeSpotLighting(int offset);


void main() {
    out_FinalColor = computeLighting() + materialEmissiveColor;
}

float u_LightBufferGet(int index) {
    return u_LightBuffer[index / 4][index % 4];
}

vec4 computeLighting() {

    vec4 result = vec4(0.0f);
    int offset = 1;

    float lightsCount = u_LightBufferGet(0);

    for(float i = 0.0f;i < lightsCount;++i) {

        float lightType = u_LightBufferGet(offset++);

        if(lightType == LIGHT_TYPE_DIRECTIONAL) {

            result += computeDirectionalLighting(offset);
            offset += DIRECTIONAL_LIGHT_SIZE;

        } else if(lightType == LIGHT_TYPE_POINT) {

            result += computePointLighting(offset);
            offset += POINT_LIGHT_SIZE;

        } else if(lightType == LIGHT_TYPE_SPOT) {

            result += computeSpotLighting(offset);
            offset += SPOT_LIGHT_SIZE;
        }
    }

    return result;
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

vec4 computeAmbientColor(vec4 lightColor) {
	return vec4(vec3(lightColor * materialAmbientColor), materialAmbientColor.a);
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


vec4 computeDirectionalLighting(int offset) {

    DirectionalLight light = DirectionalLight(

        vec3(u_LightBufferGet(offset), u_LightBufferGet(offset+1), u_LightBufferGet(offset+2)),
        vec4(u_LightBufferGet(offset+3), u_LightBufferGet(offset+4), u_LightBufferGet(offset+5), u_LightBufferGet(offset+6))
    );

    vec3 direction = normalize(-light.direction);

    return computeAmbientColor(light.color)
         + computeDiffuseColor(light.color, direction)
         + computeSpecularColor(light.color, direction);
}


vec4 computePointLighting(int offset) {

    PointLight light = PointLight(

        vec3(u_LightBufferGet(offset), u_LightBufferGet(offset+1), u_LightBufferGet(offset+2)),
        vec4(u_LightBufferGet(offset+3), u_LightBufferGet(offset+4), u_LightBufferGet(offset+5), u_LightBufferGet(offset+6)),
        u_LightBufferGet(offset+7),
        u_LightBufferGet(offset+8),
        u_LightBufferGet(offset+9)
    );

    vec3 direction = normalize(light.position - vertexData.position);

    float attenuation = computeAttenuation(light.position, light.constant, light.linear, light.quadratic);

    return (computeAmbientColor(light.color)
         + computeDiffuseColor(light.color, direction)
         + computeSpecularColor(light.color, direction)) * attenuation;
}


vec4 computeSpotLighting(int offset) {

    SpotLight light = SpotLight(

        vec3(u_LightBufferGet(offset), u_LightBufferGet(offset+1), u_LightBufferGet(offset+2)),
        vec3(u_LightBufferGet(offset+3), u_LightBufferGet(offset+4), u_LightBufferGet(offset+5)),
        vec4(u_LightBufferGet(offset+6), u_LightBufferGet(offset+7), u_LightBufferGet(offset+8), u_LightBufferGet(offset+9)),
        u_LightBufferGet(offset+10),
        u_LightBufferGet(offset+11),
        u_LightBufferGet(offset+12),
        u_LightBufferGet(offset+13),
        u_LightBufferGet(offset+14)
    );

    vec3 direction = normalize(light.position - vertexData.position);

    float attenuation = computeAttenuation(light.position, light.constant, light.linear, light.quadratic);

    float intensity = computeIntensity(direction, light.direction, light.cutOff, light.outerCutOff);

    return (computeAmbientColor(light.color)
         + computeDiffuseColor(light.color * intensity, direction)
         + computeSpecularColor(light.color * intensity, direction)) * attenuation;
}
