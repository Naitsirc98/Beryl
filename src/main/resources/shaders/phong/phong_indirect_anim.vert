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

layout(std430, binding = 4) readonly buffer Bones {
    mat4 u_BoneTransformations[];
};

uniform vec4 u_ClipPlane;

layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;
layout(location = 3) in ivec4 in_BoneIndices;
layout(location = 4) in vec4 in_BoneWeights;
layout(location = 5) in int in_TransformIndex;
layout(location = 6) in int in_MaterialIndex;

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

    vec4 position = vec4(in_Position, 1.0);
    vec4 normal = vec4(in_Normal, 0.0);

    for(int i = 0;i < 4;i++) {

        int boneIndex = in_BoneIndices[i];
        float weight = in_BoneWeights[i];

        mat4 transformation = u_BoneTransformations[boneIndex];

        position += weight * (transformation * vec4(in_Position, 1.0));

        normal += weight * (transformation * vec4(in_Normal, 0.0));
    }

    Transform transform = u_Transforms[in_TransformIndex];

    position = transform.modelMatrix * position;

    gl_ClipDistance[0] = dot(position, u_ClipPlane);

    vertexData.position = position.xyz;
    vertexData.normal = normalize(mat3(transform.normalMatrix) * normal.xyz);
    vertexData.texCoords = in_TexCoords;
    vertexData.materialIndex = in_MaterialIndex;


    gl_Position = u_Camera.projectionViewMatrix * position;
}