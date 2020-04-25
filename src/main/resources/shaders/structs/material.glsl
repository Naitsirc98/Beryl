#define MATERIAL_SIZEOF 144

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

    vec2 texCoordsFactor;

    float alpha;
    float shininess;
    float reflectivity;
    float refractiveIndex;

    int flags;
};


struct MetallicMaterial {

    vec4 color;
    vec4 emissiveColor;
    vec4 _padding0;
    vec4 _padding1;

    layout(bindless_sampler) sampler2D colorMap;
    layout(bindless_sampler) sampler2D metallicRoughnessMap;
    layout(bindless_sampler) sampler2D emissiveMap;
    layout(bindless_sampler) sampler2D occlusionMap;
    layout(bindless_sampler) sampler2D normalMap;
    layout(bindless_sampler) sampler2D _padding2;

    vec2 texCoordsFactor;

    float alpha;
    float metallic;
    float roughness;
    float fresnel;

    int flags;
};

struct SpecularMaterial {

    vec4 diffuseColor;
    vec4 specularColor;
    vec4 emissiveColor;
    vec4 _padding0;

    layout(bindless_sampler) sampler2D diffuseMap;
    layout(bindless_sampler) sampler2D specularGlossinessMap;
    layout(bindless_sampler) sampler2D emissiveMap;
    layout(bindless_sampler) sampler2D occlusionMap;
    layout(bindless_sampler) sampler2D normalMap;
    layout(bindless_sampler) sampler2D _padding1;

    vec2 texCoordsFactor;

    float alpha;
    float glossiness;
    float fresnel;
    float _padding2;

    int flags;
};


// === FLAGS === //

bool testMaterialFlag(int materialFlags, int flag) {
    return (materialFlags & flag) == flag;
}

#define NORMAL_MAP_PRESENT 0x1