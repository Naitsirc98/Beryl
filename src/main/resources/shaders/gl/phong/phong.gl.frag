#version 330 core

struct PhongMaterial {

    vec4 ambientColor;
    vec4 diffuseColor;
    vec4 specularColor;
    vec4 emissiveColor;
    sampler2D ambientMap;
    sampler2D diffuseMap;
    sampler2D specularMap;
    sampler2D emissiveMap;
};

uniform PhongMaterial u_Material;

in vec2 frag_TextureCoords;

out vec4 out_FinalColor;

void main() {
    out_FinalColor = u_Material.diffuseColor * texture2D(u_Material.diffuseMap, frag_TextureCoords);
}