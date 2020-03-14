#version 450 core

@include "structs/phong_material.glsl"

layout(binding = 0) uniform Material {
    PhongMaterial u_Material;
};

layout(location = 0) in vec2 frag_TextureCoords;

layout(location = 0) out vec4 out_FinalColor;

void main() {
    out_FinalColor = u_Material.diffuseColor * texture2D(u_Material.diffuseMap, frag_TextureCoords);
}