/*
The BRDF, or bidirectional reflective distribution function is a function that takes as input the incoming
(light) direction w_i, the outgoing (view) direction w_o, the surface normal n and a surface parameter a that
represents the microsurface’s roughness. The BRDF approximates how much each individual light ray w_i
contributes to the final reflected light of an opaque surface given its material properties.

https://learnopengl.com/PBR/Theory
*/

#version 410 core

layout(location = 0) in frag_TextureCoords;

layout(location = 0) out out_FinalColor;


@include "pbr/functions.glsl"


vec2 integrateBRDF(float NdotV, float roughness) {

    vec3 V;
    V.x = sqrt(1.0 - NdotV * NdotV);
    V.y = 0.0;
    V.z = NdotV;

    float A = 0.0;
    float B = 0.0;

    vec3 N = vec3(0.0, 0.0, 1.0);

    const uint SAMPLE_COUNT = 1024u;

    for(uint i = 0u; i < SAMPLE_COUNT; ++i) {
        // Generates a sample vector that's biased towards the
        // preferred alignment direction (importance sampling).
        vec2 Xi = hammersley(i, SAMPLE_COUNT);
        vec3 H = importanceSampleGGX(Xi, N, roughness);
        vec3 L = normalize(2.0 * dot(V, H) * H - V);

        float NdotL = max(L.z, 0.0);
        float NdotH = max(H.z, 0.0);
        float VdotH = max(dot(V, H), 0.0);

        if(NdotL > 0.0) {
            float G = geometrySmith(N, V, L, roughness);
            float G_Vis = (G * VdotH) / (NdotH * NdotV);
            float Fc = pow(1.0 - VdotH, 5.0);

            A += (1.0 - Fc) * G_Vis;
            B += Fc * G_Vis;
        }
    }

    A /= float(SAMPLE_COUNT);
    B /= float(SAMPLE_COUNT);

    return vec2(A, B);
}

void main() {

    vec2 integratedBRDF = integrateBRDF(frag_TextureCoords.x, frag_TextureCoords.y);
   
    out_FinalColor = integratedBRDF;
}