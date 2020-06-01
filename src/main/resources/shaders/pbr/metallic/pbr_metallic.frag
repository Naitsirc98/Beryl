#version 450 core

#extension GL_ARB_bindless_texture: require

#define MAX_POINT_LIGHTS 10
#define MAX_SPOT_LIGHTS 10

@include "structs/lights.glsl"
@include "structs/metallic_material.glsl"
@include "structs/fog.glsl"
@include "structs/shadow_cascade.glsl"
@include "structs/skybox_pbr.glsl"

layout(std140, binding = 0) uniform Camera {
    mat4 projectionViewMatrix;
    vec4 position;
} u_Camera;


layout(std140, binding = 1) uniform Lights {
    Light u_DirectionalLight;
    Light u_PointLights[MAX_POINT_LIGHTS];
    Light u_SpotLights[MAX_SPOT_LIGHTS];
    vec4 u_AmbientColor;
    Fog u_Fog;
    int u_PointLightsCount;
    int u_SpotLightsCount;
};

layout(std430, binding = 3) readonly buffer Materials {
    MetallicMaterial u_Materials[];
};

layout(std140, binding = 5) uniform ShadowsInfo {
    ShadowCascade u_ShadowCascades[MAX_SHADOW_CASCADES_COUNT];
    bool u_ShadowsEnabled;
};

layout(std140, binding = 6) uniform SkyboxInfo {
    Skybox u_Skybox;
    bool u_SkyboxPresent;
};

layout(location = 0) in FragmentData {
    vec3 position;
    vec3 normal;
    vec2 texCoords;
    flat int materialIndex;
    vec4 positionDirLightSpace[MAX_SHADOW_CASCADES_COUNT];
} fragment;


layout(location = 0) out vec4 out_FragmentColor;

@include "pbr/functions.glsl"
@include "pbr/metallic/pbr_metallic_lighting.glsl"


MetallicMaterial material;

PBRMetallicInfo info;


vec3 computeDirLights() {

    if(u_DirectionalLight.type == NULL) {
        return vec3(0.0);
    }

    vec3 L = normalize(u_DirectionalLight.direction.xyz);
    vec3 H = normalize(info.viewDirection + L);
    float distance = length(u_DirectionalLight.direction.xyz);
    float attenuation = 1.0; // No attenuation in directional lights

    vec3 L0 = calculateLighting(u_DirectionalLight.color.rgb, L, H, attenuation, info);

    return L0;
}

vec3 computePointLights() {

    vec3 L0 = vec3(0.0);

    for(int i = 0; i < u_PointLightsCount; ++i) {

        Light light = u_PointLights[i];

        vec3 direction = light.position.xyz - info.fragmentPosition;

        vec3 L = normalize(direction);
        vec3 H = normalize(info.viewDirection + L);
        float distance = length(direction);
        float attenuation = 1.0 / (distance * distance);

        L0 += calculateLighting(light.color.rgb, L, H, attenuation, info);
    }

    return L0;
}

vec3 reflectanceEquation() {

    vec3 dirLighting = computeDirLights();
    
    vec3 pointLighting = computePointLights();
          
    vec3 lighting = dirLighting + pointLighting;

    return lighting;
}

vec4 applyFogEffect(vec4 fragmentColor) {

    vec3 fogColor = u_Fog.color.rgb * (u_AmbientColor.rgb * u_DirectionalLight.color.rgb);

    float distance = length(u_Camera.position.xyz - fragment.position) / 100.0;

    float exponent = distance * u_Fog.density;

    if(exponent == 0.0) {
        return fragmentColor;
    }

    float fogFactor = 1.0 / exp(exponent * exponent);

    fogFactor = clamp(fogFactor, 0.0, 1.0);

    vec3 finalColor = mix(fogColor, fragmentColor.rgb, fogFactor);

    return vec4(finalColor, fragmentColor.a);
}

vec3 getDiffuseIBL() {
    return info.albedo * texture(u_Skybox.irradianceMap, info.normal).rgb;
}

vec3 getSpecularIBL(vec3 F, float angle) {
    // Sample both the pre-filter map and the BRDF lut and combine them together as per the Split-Sum approximation to get the IBL specular part.
    float prefilterLOD = info.roughness * u_Skybox.maxPrefilterLOD + u_Skybox.prefilterLODBias;
    vec3 prefilteredColor = textureLod(u_Skybox.prefilterMap, info.reflectDirection, prefilterLOD).rgb;
    vec2 brdf = texture(u_Skybox.brdfMap, vec2(angle, info.roughness)).rg;
    return prefilteredColor * (F * brdf.x + brdf.y);    
}

vec4 computeLighting() {

    vec2 texCoords = fragment.texCoords * material.tiling;

    // Material properties
    info.albedo = getAlbedo(material, texCoords).rgb;
    info.metallic = getMetallic(material, texCoords);
    info.roughness = getRoughness(material, texCoords);
    info.occlusion = getOcclusion(material, texCoords);
    info.normal = getNormal(material, texCoords, fragment.position, fragment.normal);
    info.F0 = getF0(material, info.albedo, info.metallic);

    info.fragmentPosition = fragment.position;

    info.viewDirection = normalize(u_Camera.position.xyz - fragment.position);
    info.reflectDirection = reflect(-info.viewDirection, info.normal);

    float angle = max(dot(info.normal, info.viewDirection), 0.0);

    // Reflectance equation
    vec3 L0 = reflectanceEquation();

    // ambient lighting (we now use IBL as the ambient term)
    vec3 F = fresnelSchlickRoughness(angle, info.F0, info.roughness);

    float shadows = 0.0;

    if(u_ShadowsEnabled) {
        shadows = computeDirShadows(u_Camera.projectionViewMatrix, fragment.position, fragment.positionDirLightSpace, u_ShadowCascades);
    }

    vec3 kS = F;
    vec3 kD = 1.0 - kS;
    kD = kD * (1.0 - shadows) * (1.0 - info.metallic);

    vec3 ambient;

    if(u_SkyboxPresent) {
        // If skybox is present, then apply Image Based Lighting (IBL)
        ambient = (kD * getDiffuseIBL() + getSpecularIBL(F, angle)) * info.occlusion;
    } else {
        ambient = kD * u_AmbientColor.rgb * info.albedo * info.occlusion;
    }

    vec3 color = ambient + L0;

    // HDR tonemapping
    color = color / (color + vec3(1.0));

    // Gamma correct
    color = pow(color, vec3(1.0 / 2.2));

    return vec4(color, 1.0);
}

void main() {

    material = u_Materials[fragment.materialIndex];

    vec2 texCoords = fragment.texCoords * material.tiling;

    vec4 materialEmissiveColor = material.emissiveColor * texture(material.emissiveMap, texCoords);

    vec4 fragmentColor;

    if(materialEmissiveColor.rgb != vec3(0.0)) {
        fragmentColor = materialEmissiveColor;
    } else {
        fragmentColor = computeLighting();
    }

    if(u_Fog.color.a != 0.0) {
        fragmentColor = applyFogEffect(fragmentColor);
    }

    out_FragmentColor = fragmentColor;
}
