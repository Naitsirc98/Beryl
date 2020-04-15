#version 450 core

uniform mat4 u_MVP;

layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;

out vec4 frag_ClipSpace;

void main() {
    frag_ClipSpace = u_MVP * vec4(in_Position, 1.0);
    gl_Position = frag_ClipSpace;
}