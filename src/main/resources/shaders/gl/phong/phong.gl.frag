#version 330 core

@include "structs/phong_material.glsl"

uniform PhongMaterial u_Material;

in vec2 frag_TextureCoords;

out vec4 out_FinalColor;

void main() {
    out_FinalColor = u_Material.diffuseColor * texture2D(u_Material.diffuseMap, frag_TextureCoords);
}