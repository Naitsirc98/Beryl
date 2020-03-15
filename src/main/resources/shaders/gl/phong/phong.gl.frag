#version 330 core

@include "structs/phong_material.glsl"
@include "structs/lights.glsl"

#define MAX_LIGHTS_COUNT 64
#define LIGHT_BUFFER_SIZE SPOT_LIGHT_SIZE * MAX_LIGHTS_COUNT

uniform vec3 u_CameraPosition;

uniform MaterialUniformBuffer {
    PhongMaterial u_Material;
};

uniform sampler2D u_AmbientMap;
uniform sampler2D u_DiffuseMap;
uniform sampler2D u_SpecularMap;
uniform sampler2D u_EmissiveMap;

uniform LightsUniformBuffer {

    int u_LightsCount;

    float u_LightBuffer[LIGHT_BUFFER_SIZE];
};

in VertexData {
    vec3 position;
    vec3 normal;
    vec2 textureCoords;
} vertexData;

out vec4 out_FinalColor;

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
    out_FinalColor = computeLighting();
}

vec4 computeLighting() {

    vec4 result;
    int offset = 0;

    for(int i = 0;i < u_LightsCount;++i) {

        float lightType = u_LightBuffer[offset++];

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
	return lightColor * materialAmbientColor;
}

vec4 computeDiffuseColor(vec4 lightColor, vec3 lightDirection) {

	float diffuse = computeAngle(vertexData.normal, lightDirection);

	return lightColor * (diffuse * materialDiffuseColor);
}

vec4 computeSpecularColor(vec4 lightColor, vec3 lightDirection) {
    
	vec3 reflectDir = reflect(-lightDirection, vertexData.normal);

	float specular = pow(computeAngle(cameraDirection, reflectDir), u_Material.shininess);

	return lightColor * (specular * materialSpecularColor);
}


vec4 computeDirectionalLighting(int offset) {

    DirectionalLight light = DirectionalLight(

        vec3(u_LightBuffer[offset], u_LightBuffer[offset+1], u_LightBuffer[offset+2]),
        vec4(u_LightBuffer[offset+3], u_LightBuffer[offset+4], u_LightBuffer[offset+5], u_LightBuffer[offset+6])
    );

    vec3 direction = normalize(-light.direction);

    return computeAmbientColor(light.color)
         + computeDiffuseColor(light.color, direction)
         + computeSpecularColor(light.color, direction);
}


vec4 computePointLighting(int offset) {

    PointLight light = PointLight(

        vec3(u_LightBuffer[offset], u_LightBuffer[offset+1], u_LightBuffer[offset+2]),
        vec4(u_LightBuffer[offset+3], u_LightBuffer[offset+4], u_LightBuffer[offset+5], u_LightBuffer[offset+6]),
        u_LightBuffer[offset+7],
        u_LightBuffer[offset+8],
        u_LightBuffer[offset+9]
    );

    vec3 direction = normalize(light.position - vertexData.position);

    float attenuation = computeAttenuation(light.position, light.constant, light.linear, light.quadratic);

    vec4 lightColor = light.color * attenuation;

    return computeAmbientColor(lightColor)
         + computeDiffuseColor(lightColor, direction)
         + computeSpecularColor(lightColor, direction);
}


vec4 computeSpotLighting(int offset) {

    SpotLight light = SpotLight(

        vec3(u_LightBuffer[offset], u_LightBuffer[offset+1], u_LightBuffer[offset+2]),
        vec3(u_LightBuffer[offset+3], u_LightBuffer[offset+4], u_LightBuffer[offset+5]),
        vec4(u_LightBuffer[offset+6], u_LightBuffer[offset+7], u_LightBuffer[offset+8], u_LightBuffer[offset+9]),
        u_LightBuffer[offset+10],
        u_LightBuffer[offset+11],
        u_LightBuffer[offset+12],
        u_LightBuffer[offset+13],
        u_LightBuffer[offset+14]
    );

    vec3 direction = normalize(light.position - vertexData.position);
    
    float attenuation = computeAttenuation(light.position, light.constant, light.linear, light.quadratic);

    float intensity = computeIntensity(direction, light.direction, light.cutOff, light.outerCutOff);

    vec4 lightColor = light.color * attenuation;

    return computeAmbientColor(lightColor)
         + computeDiffuseColor(lightColor * intensity, direction)
         + computeSpecularColor(lightColor * intensity, direction);
}