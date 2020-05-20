#version 450 core

#extension GL_KHR_vulkan_glsl: require
#extension GL_ARB_bindless_texture: require

#define MAX_SHADOW_CASCADES_COUNT 3
#define MAX_POINT_LIGHTS 10
#define MAX_SPOT_LIGHTS 10

@include "structs/lights.glsl"
@include "structs/metallic_material.glsl"
@include "structs/fog.glsl"
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
    mat4 u_DirLightMatrices[MAX_SHADOW_CASCADES_COUNT];
    float u_CascadeFarPlanes[MAX_SHADOW_CASCADES_COUNT]; 
};

uniform bool u_ShadowsEnabled;

uniform sampler2D u_DirShadowMaps[MAX_SHADOW_CASCADES_COUNT];

uniform Skybox u_Skybox;


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

    vec3 L = normalize(u_DirectionalLight.direction.xyz);
    vec3 H = normalize(info.viewDirection + L);
    float distance = length(u_DirectionalLight.direction.xyz);
    float attenuation = 1.0; // No attenuation in directional lights

    vec3 L0 = calculateLighting(u_DirectionalLight.color.rgb, L, H, attenuation, info);

    return L0;
}

vec3 computePointLights() {

    vec3 L0 = vec3(0.0);

    for(uint i = 0; i < u_PointLightsCount; ++i) {

        Light light = u_PointLights[i];

        vec3 L = normalize(light.position.xyz - info.fragmentPosition);
        vec3 H = normalize(info.viewDirection + L);
        float distance = length(light.position.xyz - info.fragmentPosition);
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

float computeDirShadows() {

    int depthMapIndex = 0;

    float fragmentDepth = (u_Camera.projectionViewMatrix * vec4(fragment.position, 1.0)).z;

    // Select the correct cascade shadow map for this fragment
    for(int i = 0; i < MAX_SHADOW_CASCADES_COUNT; i++) {
        if(fragmentDepth < u_CascadeFarPlanes[i]) {
            depthMapIndex = i;
            break;
        }
    }

    // Transform from screen coordinates to texture coordinates
    vec4 positionDirLightSpace = fragment.positionDirLightSpace[depthMapIndex];

    vec3 projCoords = positionDirLightSpace.xyz;// / positionDirLightSpace.w;

    projCoords = projCoords * 0.5 + 0.5;

    if(projCoords.z > 1.0) {
        return 0.0;
    }

    float bias = 0.005; //max(0.05 * (1.0 - dot(fragmentNormal, u_DirectionalLight.direction.xyz)), 0.005);

    float shadow = 0.0;

    float numberOfSamples = 9.0;

    sampler2D depthMap = u_DirShadowMaps[depthMapIndex];

    vec2 inc = 1.0 / textureSize(depthMap, 0);

    for(int row = -1; row <= 1; ++row) {

        for(int col = -1; col <= 1; ++col) {

            vec2 shadowSampleCoords = projCoords.xy + vec2(row, col) * inc;

            float textDepth = texture(depthMap, shadowSampleCoords).r;

            shadow += projCoords.z - bias > textDepth ? 1.0 : 0.0;
        }
    }

    shadow /= numberOfSamples;

    return shadow;
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

vec4 computeLighting() {

    // Material properties
    info.albedo = getAlbedo(material, fragment.texCoords).rgb;
    info.metallic = getMetallic(material, fragment.texCoords);
    info.roughness = getRoughness(material, fragment.texCoords);
    info.occlusion = getOcclusion(material, fragment.texCoords);
    info.normal = getNormal(material, fragment.texCoords, fragment.position, fragment.normal);
    info.F0 = getF0(material, info.albedo, info.metallic);

    info.fragmentPosition = fragment.position;

    info.viewDirection = normalize(u_Camera.position.xyz - fragment.position);
    info.reflectDirection = reflect(-info.viewDirection, info.normal);

    float angle = max(dot(info.normal, info.viewDirection), 0.0);

    // Reflectance equation
    vec3 L0 = reflectanceEquation();

    // ambient lighting (we now use IBL as the ambient term)
    vec3 F = fresnelSchlickRoughness(angle, info.F0, info.roughness);

    vec3 kS = F;
    vec3 kD = 1.0 - kS;
    kD *= 1.0 - material.metallic;

    vec3 irradiance = texture(u_Skybox.irradianceMap, info.normal).rgb;
    vec3 diffuse = irradiance * info.albedo;

    // sample both the pre-filter map and the BRDF lut and combine them together as per the Split-Sum approximation to get the IBL specular part.
    const float MAX_REFLECTION_LOD = 4.0;
    vec3 prefilteredColor = textureLod(u_Skybox.prefilterMap, info.reflectDirection, info.roughness * MAX_REFLECTION_LOD).rgb;
    vec2 brdf = texture(u_Skybox.brdfMap, vec2(angle, info.roughness)).rg;
    vec3 specular = prefilteredColor * (F * brdf.x + brdf.y);

    float shadows = 0.0;

    if(u_ShadowsEnabled) {
        shadows = computeDirShadows();
    }

    vec3 ambient = (u_AmbientColor.rgb + ((kD + (1.0 - shadows)) * (diffuse + specular))) * info.occlusion;

    vec3 color = ambient + L0;

    // HDR tonemapping
    // color = color / (color + vec3(1.0));

    // Gamma correct
    // color = pow(color, vec3(GAMMA_CORRECTION));

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
