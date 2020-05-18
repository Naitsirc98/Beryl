/*
The BRDF, or bidirectional reflective distribution function is a function that takes as input the incoming
(light) direction w_i, the outgoing (view) direction w_o, the surface normal n and a surface parameter a that
represents the microsurface’s roughness. The BRDF approximates how much each individual light ray w_i
contributes to the final reflected light of an opaque surface given its material properties.

https://learnopengl.com/PBR/Theory
*/

#version 410 core

layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec2 in_TextureCoords;

layout(location = 0) out vec2 frag_TextureCoords;

void main() {

    frag_TextureCoords = in_TextureCoords;

	gl_Position = vec4(in_Position, 1.0);
}