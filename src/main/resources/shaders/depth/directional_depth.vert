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

uniform mat4 u_LightProjectionViewMatrix;

layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;
layout(location = 3) in int in_TransformIndex;
layout(location = 4) in int in_MaterialIndex;

void main() {

    mat4 modelMatrix = u_Transforms[in_TransformIndex].modelMatrix;
    
    gl_Position = u_LightProjectionViewMatrix * modelMatrix * vec4(in_Position, 1.0);
}
