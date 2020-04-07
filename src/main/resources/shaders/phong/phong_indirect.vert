#version 450 core

#extension GL_KHR_vulkan_glsl: require

layout(std140, set = 0, binding = 0) uniform Camera {
    mat4 u_ProjectionViewMatrix;
    vec4 u_CameraPosition;
};

layout(std430, binding = 2) buffer ModelMatrices {
    mat4 u_ModelMatrix[];
};

layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;
layout(location = 3) in uint in_ModelMatrixIndex;
layout(location = 4) in uint in_MaterialIndex;

layout(location = 0) out VertexData {
    vec3 position;
    vec3 normal;
    vec2 texCoords;
    flat uint materialIndex;
} vertexData;

void main() {

    vec4 position = u_ModelMatrix[in_ModelMatrixIndex];
    
    vertexData.position = position.xyz;
    vertexData.normal = normalize(in_Normal);
    vertexData.texCoords = in_TexCoords;
    vertexData.materialIndex = in_MaterialIndex;

    gl_Position = u_ProjectionViewMatrix * position;

}