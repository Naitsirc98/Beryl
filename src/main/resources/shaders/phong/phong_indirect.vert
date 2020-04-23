#version 450 core

#extension GL_KHR_vulkan_glsl: require

@include "structs/transform.glsl"

layout(std140, set = 0, binding = 0) uniform Camera {
    mat4 projectionViewMatrix;
    vec4 position;
} u_Camera;

layout(std430, binding = 2) readonly buffer Transforms {
    Transform u_Transforms[];
};

uniform vec4 u_ClipPlane;

layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;
layout(location = 3) in int in_TransformIndex;
layout(location = 4) in int in_MaterialIndex;

layout(location = 0) out VertexData {
    vec3 position;
    vec3 normal;
    vec2 texCoords;
    flat int materialIndex;
} vertexData;

out gl_PerVertex {
    vec4 gl_Position;
    float gl_PointSize;
    float gl_ClipDistance[1];
};


void main() {


    Transform transform = u_Transforms[in_TransformIndex];

    vec4 position = transform.modelMatrix * vec4(in_Position, 1.0);

    gl_ClipDistance[0] = dot(position, u_ClipPlane);

    vertexData.position = position.xyz;
    vertexData.normal = normalize(transform.modelMatrix * vec4(in_Normal, 0.0)).xyz;
    vertexData.texCoords = in_TexCoords;
    vertexData.materialIndex = in_MaterialIndex;


    gl_Position = u_Camera.projectionViewMatrix * position;
}