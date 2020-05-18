// Quasi monte-carlo simulation on the environment lighting to create a prefilter (cube)map.

#version 410

#define SAMPLE_COUNT 1024u

uniform samplerCube u_EnvironmentMap;

uniform float u_Roughness;


layout(location = 0) in vec3 frag_WorldPosition;

layout(location = 0) out vec4 out_FinalColor;


@include "pbr/functions.glsl"


vec3 calculatePrefilteredColor(vec3 N, vec3 R, vec3 V) {

    vec3 prefilteredColor = vec3(0.0);
    float totalWeight = 0.0;

    for(uint i = 0u; i < SAMPLE_COUNT; ++i) {
        // generates a sample vector that's biased towards the preferred alignment direction (importance sampling).
        vec2 Xi = Hammersley(i, SAMPLE_COUNT);
        vec3 H = ImportanceSampleGGX(Xi, N, u_Roughness);
        vec3 L  = normalize(2.0 * dot(V, H) * H - V);

        float NdotL = max(dot(N, L), 0.0);
        if(NdotL > 0.0) {
            // sample from the environment's mip level based on u_Roughness/pdf
            float D  = distributionGGX(N, H, u_Roughness);
            float NdotH = max(dot(N, H), 0.0);
            float HdotV = max(dot(H, V), 0.0);
            float pdf = D * NdotH / (4.0 * HdotV) + 0.0001;

            float resolution = 512.0; // resolution of source cubemap (per face)
            float saTexel  = 4.0 * PI / (6.0 * resolution * resolution);
            float saSample = 1.0 / (float(SAMPLE_COUNT) * pdf + 0.0001);

            float mipLevel = u_Roughness == 0.0 ? 0.0 : 0.5 * log2(saSample / saTexel);

            prefilteredColor += textureLod(u_EnvironmentMap, L, mipLevel).rgb * NdotL;
            totalWeight += NdotL;
        }
    }

    prefilteredColor = prefilteredColor / totalWeight;

    return prefilteredColor;
}

void main() {

    vec3 N = normalize(frag_WorldPosition);

    // Make the simplyfying assumption that V equals R equals the normal.
    vec3 R = N;
    vec3 V = R;

    vec3 prefilteredColor = calculatePrefilteredColor(N, R, V);

    out_FinalColor = vec4(prefilteredColor, 1.0);
}






