#version 450 core

uniform mat4 u_MVP;

uniform float u_Tiling;

layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;

layout(location = 0) out FragmentData {
    vec4 clipSpace;
    vec2 textureCoords;
} fragmentData;

void main() {
    fragmentData.clipSpace = u_MVP * vec4(in_Position, 1.0);
    fragmentData.textureCoords = in_TexCoords * u_Tiling;
    gl_Position = fragmentData.clipSpace;
}