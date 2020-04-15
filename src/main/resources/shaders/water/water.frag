#version 450 core

uniform sampler2D u_ReflectionMap;
uniform sampler2D u_RefractionMap;

in vec4 frag_ClipSpace;

layout(location = 0) out vec4 out_FragmentColor;

void main() {

    vec2 ndc = (frag_ClipSpace.xy / frag_ClipSpace.w) / 2.0 + 0.5;

    vec4 reflectionColor = texture(u_ReflectionMap, vec2(ndc.x, -ndc.y));
    vec4 refractionColor = texture(u_RefractionMap, ndc);

    out_FragmentColor = mix(reflectionColor, refractionColor, 0);
}