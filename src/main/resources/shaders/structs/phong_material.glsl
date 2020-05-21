struct PhongMaterial {

    vec4 ambientColor;
    vec4 diffuseColor;
    vec4 specularColor;
    vec4 emissiveColor;

    layout(bindless_sampler) sampler2D ambientMap;
    layout(bindless_sampler) sampler2D diffuseMap;
    layout(bindless_sampler) sampler2D specularMap;
    layout(bindless_sampler) sampler2D emissiveMap;
    layout(bindless_sampler) sampler2D occlusionMap;
    layout(bindless_sampler) sampler2D normalMap;

    vec2 tiling;

    float alpha;
    float shininess;
    float reflectivity;
    float refractiveIndex;

    int flags;

    float _padding;
};

// FLAGS
#define NORMAL_MAP_PRESENT 0x1

@include "structs/materials_base.glsl"