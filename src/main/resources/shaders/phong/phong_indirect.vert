#version 450 core

#extension GL_KHR_vulkan_glsl: require

layout(std140, set = 0, binding = 0) uniform Camera {
    mat4 projectionViewMatrix;
    vec4 position;
} u_Camera;

layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;

layout(std430, binding = 2) readonly buffer ModelMatrices {
    mat4 u_ModelMatrices[];
};

layout(location = 0) out VertexData {
    vec3 position;
    vec3 normal;
    vec2 texCoords;
    flat int materialIndex;
} vertexData;


void main() {

    vec4 position = u_ModelMatrices[0] * vec4(in_Position, 1.0);

    vertexData.position = position.xyz;
    vertexData.normal = normalize(in_Normal);
    vertexData.texCoords = in_TexCoords;
    vertexData.materialIndex = 0;


    gl_Position = u_Camera.projectionViewMatrix * position;
}