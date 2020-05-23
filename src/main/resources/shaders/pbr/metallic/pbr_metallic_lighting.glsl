// NOTE: pbr/functions.glsl must be included before this

struct PBRMetallicInfo {

    vec3 albedo;
    float metallic;
    float roughness;
    float occlusion;
    vec3 normal;
    vec3 F0;

    vec3 fragmentPosition;

    vec3 viewDirection;
    vec3 reflectDirection;
};

vec3 calculateLighting(vec3 lightColor, vec3 L, vec3 H, float attenuation, PBRMetallicInfo info) {

    vec3 fragPosition = info.fragmentPosition;
    vec3 normal = info.normal;
    vec3 viewDir = info.viewDirection;
    vec3 F0 = info.F0;
    vec3 albedo = info.albedo;
    float metallic = info.metallic;
    float roughness = info.roughness;

    // Calculate per-light radiance
    vec3 radiance = lightColor * attenuation;

    // Cook-Torrance BRDF
    float NDF = distributionGGX(normal, H, roughness);
    float G = geometrySmith(normal, viewDir, L, roughness);
    vec3 F = fresnelSchlick(max(dot(H, normal), 0.0), F0);

    vec3 nominator = NDF * G * F;
    float denominator = 4.0 * max(dot(normal, viewDir), 0.0) * max(dot(normal, L), 0.0);
    vec3 specular = nominator / (denominator + 0.0001); // 0.001 to prevent division by zero.

    // kS is equal to Fresnel
    vec3 kS = F;
    // for energy conservation, the diffuse and specular light can't
    // be above 1.0 (unless the surface emits light); to preserve this
    // relationship the diffuse component (kD) should equal 1.0 - kS.
    vec3 kD = vec3(1.0) - kS;
    // multiply kD by the inverse metalness such that only non-metals
    // have diffuse lighting, or a linear blend if partly metal (pure metals
    // have no diffuse light).
    kD *= 1.0 - metallic;

    // scale light by NdotL
    float NdotL = max(dot(normal, L), 0.0);

    // add to outgoing radiance Lo
    // note that we already multiplied the BRDF by the Fresnel (kS) so we won't multiply by kS again
    vec3 L0 = (kD * albedo / PI + specular) * radiance * NdotL;

    return L0;
}