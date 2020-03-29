@beryl

@include "structs/phong_material.glsl"
@include "structs/lights.glsl"

#define MAX_LIGHTS_COUNT 100

@include "phong.frag.uniforms"

#ifdef OPENGL

in VertexData {
    vec3 position;
    vec3 normal;
    vec2 textureCoords;
} vertexData;

#else // VULKAN

layout(location = 0) in VertexData {
    vec3 position;
    vec3 normal;
    vec2 textureCoords;
} vertexData;

#endif

#ifdef OPENGL
out vec4 out_FinalColor;
#else // VULKAN
layout(location = 0) out vec4 out_FinalColor;
#endif


vec3 cameraDirection = normalize(u_CameraPosition.xyz - vertexData.position);

vec4 materialAmbientColor = u_Material.ambientColor * texture(u_AmbientMap, vertexData.textureCoords);
vec4 materialDiffuseColor = u_Material.diffuseColor * texture(u_DiffuseMap, vertexData.textureCoords);
vec4 materialSpecularColor = u_Material.specularColor * texture(u_SpecularMap, vertexData.textureCoords);
vec4 materialEmissiveColor = u_Material.emissiveColor * texture(u_EmissiveMap, vertexData.textureCoords);

vec4 computeLighting();
vec4 computeDirectionalLighting(Light light);
vec4 computePointLighting(Light light);
vec4 computeSpotLighting(Light light);


void main() {
    out_FinalColor = computeLighting() + materialEmissiveColor;
}

vec4 computeLighting() {

    vec4 result = vec4(0.0f);

    for(int i = 0;i < u_LightsCount;++i) {

        Light light = u_Lights[i];

        switch(light.type) {

            case LIGHT_TYPE_DIRECTIONAL:
                result += computeDirectionalLighting(light);
                break;

            case LIGHT_TYPE_POINT:
                result += computePointLighting(light);
                break;

            case LIGHT_TYPE_SPOT:
                result += computeSpotLighting(light);
                break;
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
	return vec4(vec3(lightColor * materialAmbientColor * 0.2f), materialAmbientColor.a);
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

    return computeAmbientColor(light.color)
         + computeDiffuseColor(light.color, direction);
         // + computeSpecularColor(light.color, direction);
}


vec4 computePointLighting(Light light) {

    vec3 direction = normalize(light.position.xyz - vertexData.position);

    float attenuation = computeAttenuation(light.position.xyz, light.constant, light.linear, light.quadratic);

    return (computeAmbientColor(light.color)
         + computeDiffuseColor(light.color, direction)
         + computeSpecularColor(light.color, direction)) * attenuation;
}

vec4 computeSpotLighting(Light light) {

    vec3 direction = normalize(light.position.xyz - vertexData.position);

    float attenuation = computeAttenuation(light.position.xyz, light.constant, light.linear, light.quadratic);

    float intensity = computeIntensity(direction, light.direction.xyz, light.cutOff, light.outerCutOff);

    return (computeAmbientColor(light.color)
         + computeDiffuseColor(light.color * intensity, direction)
         + computeSpecularColor(light.color * intensity, direction)) * attenuation;
}
