#version 410 core

uniform mat4 u_ProjectionViewMatrix;

layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;

layout(location = 0) out vec3 frag_WorldPosition;


void main() {

    vec2 worldPos = in_Position;

    frag_WorldPosition = worldPos;

    gl_Position = u_ProjectionViewMatrix * vec4(worldPos, 1.0);
}
