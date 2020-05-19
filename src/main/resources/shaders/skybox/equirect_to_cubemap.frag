#version 410 core

uniform sampler2D u_EquirectangularMap;


layout(location = 0) in vec3 frag_WorldPosition;

layout(location = 0) out vec4 out_FinalColor;


vec2 sampleSphericalMap(vec3 v) {

    vec2 uv = vec2(atan(v.z, v.x), asin(v.y));
    // Inverse atan
    uv = uv * vec2(0.1591, 0.3183) + 0.5;

    return uv;
}

void main() {

    vec2 uv = sampleSphericalMap(normalize(frag_WorldPosition));
    
    vec3 color = texture(u_EquirectangularMap, uv).rgb;

    out_FinalColor = vec4(color, 1.0);
}
