@beryl

layout(std140, set = 0, binding = 0) uniform MatricesUniformBuffer {
    mat4 u_ProjectionViewMatrix;
    vec4 u_CameraPosition;
};

layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;
layout(location = 3) in mat4 in_ModelMatrix;
// layout(location = 7) in mat4 in_NormalMatrix;

layout(location = 0) out VertexData {
    vec3 position;
    vec3 normal;
    vec2 textureCoords;
} vertexData;

void main() {

    vec4 position = in_ModelMatrix * vec4(in_Position, 1.0f);

    vertexData.position = position.xyz;
    vertexData.normal = normalize(in_Normal);
    vertexData.textureCoords = in_TexCoords;

    gl_Position = u_ProjectionViewMatrix * position;
}
