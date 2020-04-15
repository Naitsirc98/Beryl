#version 450 core

layout(std140, set = 0, binding = 0) uniform Camera {
    mat4 projectionViewMatrix;
    vec4 position;
} u_Camera;

uniform sampler2D u_ReflectionMap;
uniform sampler2D u_RefractionMap;
uniform sampler2D u_DUDVMap;
uniform sampler2D u_NormalMap;

uniform bool u_DUDVMapPresent;
uniform bool u_NormalMapPresent;

uniform float u_DistortionStrength;
uniform float u_TextureCoordsOffset;

uniform vec4 u_WaterColor;
uniform float u_WaterColorStrength;

layout(location = 0) in FragmentData {
    vec4 clipSpace;
    vec3 position;
    vec3 normal;
    vec2 textureCoords;
} fragmentData;


layout(location = 0) out vec4 out_FragmentColor;


void main() {

    vec2 ndc = (fragmentData.clipSpace.xy / fragmentData.clipSpace.w) / 2.0 + 0.5;

    vec2 reflectionTexCoords = vec2(ndc.x, -ndc.y);
    vec2 refractionTexCoords = ndc;

    vec2 distortionTexCoords = vec2(0.0);
    vec2 distortion = vec2(0.0);

    vec3 normal = vec3(0.0);

    if(u_DUDVMapPresent) {
        
        vec2 texCoords = fragmentData.textureCoords;

        distortionTexCoords = texture(u_DUDVMap, vec2(texCoords.x + u_TextureCoordsOffset, texCoords.y)).rg * 0.1;
        distortionTexCoords = texCoords + vec2(distortionTexCoords.x, distortionTexCoords.y + u_TextureCoordsOffset);

        distortion = (texture(u_DUDVMap, distortionTexCoords).rg * 2.0 - 1.0) * u_DistortionStrength;
    }

    if(u_NormalMapPresent) {
        normal = texture(u_NormalMap, distortionTexCoords).rgb;
        normal = vec3(normal.r * 2.0 - 1.0, normal.b, normal.g * 2.0 - 1.0);
        normal = normalize(normal);
    } else {
        normal = normalize(fragmentData.normal);
    }

    reflectionTexCoords += distortion;
    reflectionTexCoords.x = clamp(reflectionTexCoords.x, 0.001, 0.999);
    reflectionTexCoords.y = clamp(reflectionTexCoords.y, -0.999, -0.001);

    refractionTexCoords += distortion;
    refractionTexCoords = clamp(refractionTexCoords, 0.001, 0.999);

    vec4 reflectionColor = texture(u_ReflectionMap, reflectionTexCoords);
    vec4 refractionColor = texture(u_RefractionMap, refractionTexCoords);

    vec3 viewDirection = normalize(u_Camera.position.xyz - fragmentData.position);
    float reflectionFactor = dot(viewDirection, normal);

    vec4 environmentColor = mix(reflectionColor, refractionColor, reflectionFactor);

    out_FragmentColor = mix(environmentColor, u_WaterColor, u_WaterColorStrength);
}