@beryl

const int MAX_WEIGHTS = 4;
const int MAX_JOINTS = 150;
const int NUM_CASCADES = 3;

uniform mat4 u_Model;
uniform mat4 u_MVP;
uniform mat4 u_NormalMatrix;

uniform mat4 u_LightMV[NUM_CASCADES];

layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;

out VertexData {
    vec3 position;
    vec3 normal;
    vec2 texCoords;
    vec4 positionLightSpace[NUM_CASCADES];
} vertexData;

void main() {

    vec4 vertexPos = u_MVP * vec4(in_Position, 1.0f);

    vertexData.position = vertexPos.xyz;
    vertexData.normal = normalize(mat3(u_NormalMatrix) * in_Normal);
    vertexData.texCoords = in_TexCoords;

    for (int i = 0; i < NUM_CASCADES; i++) {
        vertexData.positionLightSpace[i] = u_LightMV[i] * u_Model * vec4(in_Position, 1.0f);
    }

    gl_Position = vertexPos;
}
