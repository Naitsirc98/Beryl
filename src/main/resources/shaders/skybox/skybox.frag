#version 450 core

#extension GL_KHR_vulkan_glsl: require

#define LOWER_LIMIT 0.0
#define UPPER_LIMIT 0.25

@include "structs/fog.glsl"


uniform samplerCube u_SkyboxTexture1;
uniform samplerCube u_SkyboxTexture2;

uniform float u_TextureBlendFactor;

uniform vec4 u_FogColor;

layout(location = 0) in vec3 in_FragmentPosition;

layout(location = 0) out vec4 out_FinalColor;

void main()
{
    vec4 color1 = textureLod(u_SkyboxTexture1, in_FragmentPosition, 0.0);
    vec4 color2 = textureLod(u_SkyboxTexture2, in_FragmentPosition, 0.0);

    vec4 color = mix(color1, color2, u_TextureBlendFactor);

    // HDR tonemap and gamma correct
    // color /= (color + vec3(1.0));
    // color = pow(color, vec3(2.2));

    if(u_FogColor.a == 0.0) {
        out_FinalColor = color;
    } else {
        float factor = (in_FragmentPosition.y - LOWER_LIMIT) / (UPPER_LIMIT - LOWER_LIMIT);
        factor = clamp(factor, 0.0, 1.0);
        out_FinalColor = mix(u_FogColor, color, factor);
    }
}
