@beryl

const int MAX_POINT_LIGHTS = 5;
const int MAX_SPOT_LIGHTS = 5;
const int NUM_CASCADES = 3;

@include "structs/phong_material.glsl"
@include "structs/lights.glsl"

uniform PhongMaterial u_Material;

uniform sampler2D u_AmbientMap;
uniform sampler2D u_DiffuseMap;
uniform sampler2D u_SpecularMap;
uniform sampler2D u_EmissiveMap;

layout(std140) uniform LightsUniformBuffer { // Light position is assumed to be in view coordinates
    Light u_Light;
};

uniform sampler2D u_ShadowMap[NUM_CASCADES];
uniform float u_CascadeFarPlanes[NUM_CASCADES];

uniform vec4 u_CameraPosition;

in VertexData {
    vec3 position;
    vec3 normal;
    vec2 texCoords;
    vec4 positionLightSpace[NUM_CASCADES];
    mat4 modelViewMatrix;
} vertexData;

out vec4 out_FinalColor;


vec3 cameraDirection = normalize(u_CameraPosition.xyz - vertexData.position);

vec4 materialAmbientColor = vec4(1);
vec4 materialDiffuseColor = vec4(1);
vec4 materialSpecularColor = vec4(1);
vec4 materialEmissiveColor = vec4(0);


float computeAngle(vec3 v1, vec3 v2) {
    return max(dot(v1, v2), 0.0f);
}

float calcShadow(vec4 position, int idx) {

    vec3 projCoords = position.xyz;

    // Transform from screen coordinates to texture coordinates
    projCoords = projCoords * 0.5 + 0.5;
    float bias = 0.005;

    float shadowFactor = 0.0;
    vec2 inc;

    inc = 1.0 / textureSize(u_ShadowMap[idx], 0);

    for(int row = -1; row <= 1; ++row) {
        for(int col = -1; col <= 1; ++col) {
            float textDepth = texture(u_ShadowMap[idx], projCoords.xy + vec2(row, col) * inc).r;
            shadowFactor += projCoords.z - bias > textDepth ? 1.0 : 0.0;
        }
    }

    shadowFactor /= 9.0;

    if(projCoords.z > 1.0) {
        shadowFactor = 1.0;
    }

    return 1 - shadowFactor;
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

    float shininess = 32;

    vec3 viewDirection = normalize(u_CameraPosition.xyz - vertexData.position);

    vec3 reflectDirection = reflect(-lightDirection, vertexData.normal);

    vec3 halfwayDirection = normalize(lightDirection + viewDirection);

    float specular = pow(computeAngle(vertexData.normal, halfwayDirection), shininess);

	return vec4(vec3(lightColor * (specular * materialSpecularColor)), materialSpecularColor.a);
}


vec4 computeDirectionalLighting(Light light) {

    vec3 direction = normalize(-light.direction.xyz);

    return computeAmbientColor(light.color)
         + computeDiffuseColor(light.color, direction)
         + computeSpecularColor(light.color, direction);
}

void main() {

    vec4 color = computeDirectionalLighting(u_Light);

    int idx;
    for (int i=0; i<NUM_CASCADES; i++) {
        if (abs(vertexData.position.z) < u_CascadeFarPlanes[i]) {
            idx = i;
            break;
        }
    }

    float shadow = calcShadow(vertexData.positionLightSpace[idx], idx);

    // out_FinalColor = clamp(color * shadow, 0, 1);

    float r = texture(u_ShadowMap[0], vertexData.texCoords).r;

    out_FinalColor = vec4(r, r, r, 1);
}
