#define MATERIAL_SIZEOF 128

struct PhongMaterial {

    vec4 ambientColor;
    vec4 diffuseColor;
    vec4 specularColor;
    vec4 emissiveColor;

    sampler2D ambientMap;
    sampler2D diffuseMap;
    sampler2D specularMap;
    sampler2D emissiveMap;
    sampler2D occlusionMap;
    sampler2D normalMap;

    float alpha;
    float shininess;
    float reflectivity;
    float refractiveIndex;
};


struct MetallicMaterial {

    vec4 color;
    vec4 emissiveColor;
    vec4 _padding0;
    vec4 _padding1;

    sampler2D colorMap;
    sampler2D metallicRoughnessMap;
    sampler2D emissiveMap;
    sampler2D occlusionMap;
    sampler2D normalMap;
    sampler2D _padding2;

    float alpha;
    float metallic;
    float roughness;
    float fresnel;
};

struct SpecularMap {

    vec4 diffuseColor;
    vec4 specularColor;
    vec4 emissiveColor;
    vec4 _padding0;

    sampler2D diffuseMap;
    sampler2D specularGlossinessMap;
    sampler2D emissiveMap;
    sampler2D occlusionMap;
    sampler2D normalMap;
    sampler2D _padding1;

    float alpha;
    float glossiness;
    float fresnel;
    float _padding2;
};
