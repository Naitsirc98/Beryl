#version 450 core

@include "structs/phong_material.glsl"

layout(binding = 0) uniform UniformBufferObject {
    PhongMaterial u_Material;
};

layout(binding = 1) uniform sampler2D u_AmbientMap;
layout(binding = 2) uniform sampler2D u_DiffuseMap;
layout(binding = 3) uniform sampler2D u_SpecularMap;
layout(binding = 4) uniform sampler2D u_EmissiveMap;

layout(location = 0) in vec2 frag_TextureCoords;

layout(location = 0) out vec4 out_FinalColor;

void main() {
    out_FinalColor = u_Material.diffuseColor * texture2D(u_DiffuseMap, frag_TextureCoords);
}