struct MetallicMaterial {

    vec4 albedo;
    vec4 emissiveColor;

    layout(bindless_sampler) sampler2D albedoMap;
    layout(bindless_sampler) sampler2D metallicMap;
    layout(bindless_sampler) sampler2D roughnessMap;
    layout(bindless_sampler) sampler2D occlusionMap;
    layout(bindless_sampler) sampler2D emissiveMap;
    layout(bindless_sampler) sampler2D normalMap;

    vec2 tiling;

    float alpha;
    float metallic;
    float roughness;
    float occlusion;
    float fresnel0;

    int flags;
};

// FLAGS
#define METALLIC_MAP_PRESENT 0x2
#define ROUGHNESS_MAP_PRESENT 0x4
#define OCCLUSION_MAP_PRESENT 0x8
#define NORMAL_MAP_PRESENT 0x10

@include "structs/materials_base.glsl"

// Easy trick to get tangent-normals to world-space to keep PBR code simplified.
// Don't worry if you don't get what's going on; you generally want to do normal
// mapping the usual way for performance anways; I do plan make a note of this
// technique somewhere later in the normal mapping tutorial.
vec3 getNormalFromMap(MetallicMaterial material, vec2 uv, vec3 position, vec3 normal) {

    vec3 tangentNormal = texture(material.normalMap, uv).xyz * 2.0 - 1.0;

    vec3 Q1 = dFdx(position);
    vec3 Q2 = dFdy(position);
    vec2 st1 = dFdx(uv);
    vec2 st2 = dFdy(uv);

    vec3 N = normalize(normal);
    vec3 T = normalize(Q1 * st2.t - Q2 * st1.t);
    vec3 B = -normalize(cross(N, T));
    mat3 TBN = mat3(T, B, N);

    return normalize(TBN * tangentNormal);
 }

 vec3 getNormal(MetallicMaterial material, vec2 uv, vec3 position, vec3 normal) {
     return getNormalFromMap(material, uv, position, normal);
     /*
     return testMaterialFlag(material.flags, NORMAL_MAP_PRESENT)
         ? getNormalFromMap(material, uv, position, normal)
         : normalize(normal);
         */
 }

vec4 getAlbedo(MetallicMaterial material, vec2 uv) {
    return material.albedo * pow(texture(material.albedoMap, uv), vec4(2.2));
}

float getMetallic(MetallicMaterial material, vec2 uv) {

    return testMaterialFlag(material.flags, METALLIC_MAP_PRESENT)
        ? texture(material.metallicMap, uv).r
        : material.metallic;
}

float getRoughness(MetallicMaterial material, vec2 uv) {

    return testMaterialFlag(material.flags, ROUGHNESS_MAP_PRESENT)
        ? texture(material.roughnessMap, uv).r
        : material.roughness;
}

float getOcclusion(MetallicMaterial material, vec2 uv) {

    return testMaterialFlag(material.flags, OCCLUSION_MAP_PRESENT)
        ? texture(material.occlusionMap, uv).r
        : material.occlusion;
}

// Calculate reflectance at vs_in.normal incidence; if dia-electric (like plastic) use the material's fresnel value,
// and if it's a metal, use the albedo color as F0 (metallic workflow)
vec3 getF0(MetallicMaterial material, vec3 albedo, float metallic) {
    return mix(vec3(material.fresnel0), albedo, metallic);
}