struct WaterMaterial {

    vec4 color;

    sampler2D dudvMap;
    sampler2D normalMap;
    sampler2D reflectionMap;
    sampler2D refractionMap;

    vec2 tiling;
    float _padding0;
    float _padding1;

    float distortionStrength;
    float textureOffset;
    float colorStrength;
    float _padding2;

    int flags;
};

// FLAGS

#define NORMAL_MAP_PRESENT 0x1
#define DUDV_MAP_PRESENT 0x2

// === //

@include "structs/materials_base.glsl"