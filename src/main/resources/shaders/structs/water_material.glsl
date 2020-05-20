struct WaterMaterial {

    vec4 color;

    layout(bindless_sampler) sampler2D dudvMap;
    layout(bindless_sampler) sampler2D normalMap;
    layout(bindless_sampler) sampler2D reflectionMap;
    layout(bindless_sampler) sampler2D refractionMap;

    float distortionStrength;
    float textureOffset;
    float colorStrength;
    float _padding;

    int flags;
};

// FLAGS

#define NORMAL_MAP_PRESENT 0x1
#define DUDV_MAP_PRESENT 0x2

// === //

@include "structs/materials_base.glsl"