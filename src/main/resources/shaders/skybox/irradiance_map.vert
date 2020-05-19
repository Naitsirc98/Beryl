#version 450 core

uniform mat4 u_ProjectionViewMatrix;

layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;

layout(location = 0) out FragmentData {
    vec3 position;
    vec3 normal;
    vec2 texCoords;
} fragment;


void main() {

    fragment.position = in_Position;
    fragment.normal = normalize(in_Normal);
    fragment.texCoords = in_TexCoords;

    gl_Position = u_ProjectionViewMatrix * vec4(in_Position, 1.0);
}