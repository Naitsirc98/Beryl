#version 330 core

uniform mat4 u_MVP;

layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;

out VertexData {
    vec3 position;
    vec3 normal;
    vec2 textureCoords;
} vertexData;

void main() {

    vertexData.position = in_Position;
    vertexData.normal = in_Normal;
    vertexData.textureCoords = in_TexCoords;

    gl_Position = u_MVP * vec4(in_Position, 1.0f);
}