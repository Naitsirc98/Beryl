#version 450 core

#extension GL_KHR_vulkan_glsl: require

@include "structs/transform.glsl"

layout(std140, set = 0, binding = 0) uniform Camera {
    mat4 projectionViewMatrix;
    vec4 position;
} u_Camera;

layout(std140, binding = 1) uniform Transform {
    Transform u_Transform;
};

layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;

layout(location = 0) out VertexData {
    vec3 position;
    vec3 normal;
    vec2 texCoords;
} vertexData;


void main() {

    vec4 position = u_Transform.modelMatrix * vec4(in_Position, 1.0);

    vertexData.position = position.xyz;
    vertexData.normal = normalize(mat3(u_Transform.normalMatrix) * in_Normal);
    vertexData.texCoords = in_TexCoords;

    gl_Position = u_Camera.projectionViewMatrix * position;
}