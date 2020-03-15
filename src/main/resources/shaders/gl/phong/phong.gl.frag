#version 330 core

@include "structs/phong_material.glsl"

uniform MaterialUniformBuffer {
    PhongMaterial u_Material;
};

uniform sampler2D u_AmbientMap;
uniform sampler2D u_DiffuseMap;
uniform sampler2D u_SpecularMap;
uniform sampler2D u_EmissiveMap;

in vec2 frag_TextureCoords;

out vec4 out_FinalColor;

void main() {
    out_FinalColor = u_Material.diffuseColor * texture2D(u_DiffuseMap, frag_TextureCoords);
}