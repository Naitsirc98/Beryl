#version 330 core

uniform mat4 u_MVP;

layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;

out vec2 frag_TextureCoords;

void main() {
    frag_TextureCoords = in_TexCoords;
    gl_Position = u_MVP * vec4(in_Position, 1.0f);
}