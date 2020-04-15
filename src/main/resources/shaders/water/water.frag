#version 450 core

uniform sampler2D u_ReflectionMap;
uniform sampler2D u_RefractionMap;
uniform sampler2D u_DUDVMap;
// uniform sampler2D u_NormalMap;

uniform bool u_DUDVMapPresent;
// uniform bool u_NormalMapPresent;

uniform float u_ReflectionStrength;

uniform float u_DistortionStrength;
uniform float u_TextureCoordsOffset;

uniform vec4 u_WaterColor;
uniform float u_WaterColorStrength;

layout(location = 0) in FragmentData {
    vec4 clipSpace;
    vec2 textureCoords;
} fragmentData;


layout(location = 0) out vec4 out_FragmentColor;


vec2 distortion(vec2 texCoords) {
    vec2 distortion = texture(u_DUDVMap, texCoords).rg * 2.0 - 1.0;
    return distortion * u_DistortionStrength;
}

vec2 combinedDistortion()  {

    vec2 texCoords = fragmentData.textureCoords;

    vec2 distortion1TexCoords = vec2(texCoords.x + u_TextureCoordsOffset, texCoords.y);
    vec2 distortion2TexCoords = vec2(-texCoords.x + u_TextureCoordsOffset, texCoords.y + u_TextureCoordsOffset);

    return distortion(distortion1TexCoords) + distortion(distortion2TexCoords);
}

void main() {

    vec2 ndc = (fragmentData.clipSpace.xy / fragmentData.clipSpace.w) / 2.0 + 0.5;

    vec2 reflectionTexCoords = vec2(ndc.x, -ndc.y);
    vec2 refractionTexCoords = ndc;

    vec2 distortion = u_DUDVMapPresent ? combinedDistortion() : vec2(0.0);

    reflectionTexCoords += distortion;
    reflectionTexCoords.x = clamp(reflectionTexCoords.x, 0.001, 0.999);
    reflectionTexCoords.y = clamp(reflectionTexCoords.y, -0.999, -0.001);

    refractionTexCoords += distortion;
    refractionTexCoords = clamp(refractionTexCoords, 0.001, 0.999);

    vec4 reflectionColor = texture(u_ReflectionMap, reflectionTexCoords);
    vec4 refractionColor = texture(u_RefractionMap, refractionTexCoords);

    vec4 environmentColor = mix(reflectionColor, refractionColor, u_ReflectionStrength);

    out_FragmentColor = mix(environmentColor, u_WaterColor, u_WaterColorStrength);
}