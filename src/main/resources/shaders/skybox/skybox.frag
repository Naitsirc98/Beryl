#version 450 core

#extension GL_KHR_vulkan_glsl: require

#define LOWER_LIMIT 0.0
#define UPPER_LIMIT 0.3

@include "structs/fog.glsl"


uniform samplerCube u_SkyboxTexture;

uniform vec3 u_FogColor;

layout(location = 0) in vec3 in_FragmentPosition;

layout(location = 0) out vec4 out_FinalColor;

void main()
{
    vec4 color = textureLod(u_SkyboxTexture, in_FragmentPosition, 0.0);

    // HDR tonemap and gamma correct
    // color /= (color + vec3(1.0));
    // color = pow(color, vec3(2.2));

    float factor = (in_FragmentPosition.y - LOWER_LIMIT) / (UPPER_LIMIT - LOWER_LIMIT);
    factor = clamp(factor, 0.0, 1.0);

    out_FinalColor = mix(vec4(u_FogColor, 1.0), color, factor);
}
