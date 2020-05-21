#version 450 core

@include "structs/water_material.glsl"

layout(std140, set = 0, binding = 0) uniform Camera {
    mat4 projectionViewMatrix;
    vec4 position;
} u_Camera;

uniform mat4 u_ModelMatrix;

uniform WaterMaterial u_Material;


layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;

layout(location = 0) out FragmentData {
    vec4 clipSpace;
    vec3 position;
    vec3 normal;
    vec2 textureCoords;
} fragmentData;


void main() {

    vec4 worldPosition = u_ModelMatrix * vec4(in_Position, 1.0);

    fragmentData.clipSpace = u_Camera.projectionViewMatrix * worldPosition;
    fragmentData.position = worldPosition.xyz;
    fragmentData.normal = normalize(in_Normal);
    fragmentData.textureCoords = in_TexCoords * u_Material.tiling;
    
    gl_Position = fragmentData.clipSpace;
}