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

