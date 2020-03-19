@beryl

@include "phong.vert.uniforms"

layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;

#ifdef OPENGL

out VertexData {
    vec3 position;
    vec3 normal;
    vec2 textureCoords;
} vertexData;

#else // VULKAN

layout(location = 0) out VertexData {
    vec3 position;
    vec3 normal;
    vec2 textureCoords;
} vertexData;

#endif

void main() {

    vertexData.position = vec3(u_ModelMatrix * vec4(in_Position, 1.0f));
    vertexData.normal = normalize(mat3(u_NormalMatrix) * in_Normal);
    vertexData.textureCoords = in_TexCoords;

    gl_Position = u_MVP * vec4(in_Position, 1.0f);
}
