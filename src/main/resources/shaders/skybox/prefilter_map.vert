#version 410

uniform mat4 u_ProjectionViewMatrix;


layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;

layout(location = 0) out vec3 frag_WorldPosition;


void main() {

    frag_WorldPosition = in_Position;

    gl_Position = u_ProjectionViewMatrix * vec4(in_Position, 1.0);
}