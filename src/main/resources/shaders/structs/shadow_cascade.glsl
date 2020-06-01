#define MAX_SHADOW_CASCADES_COUNT 3

struct ShadowCascade {
    mat4 lightMatrix;
    layout(bindless_sampler) sampler2D depthMap;  
    float farPlane;
    float _padding;
};

float computeDirShadows(mat4 projectionViewMatrix, vec3 fragPos, vec4 posDirLightSpace[MAX_SHADOW_CASCADES_COUNT],
     ShadowCascade shadowCascades[MAX_SHADOW_CASCADES_COUNT]) {

    int shadowCascadeIndex = 0;

    float fragmentDepth = (projectionViewMatrix * vec4(fragPos, 1.0)).z;

    // Select the correct cascade shadow map for this fragment
    for(int i = 0; i < MAX_SHADOW_CASCADES_COUNT; i++) {
        if(fragmentDepth < shadowCascades[i].farPlane) {
            shadowCascadeIndex = i;
            break;
        }
    }

    ShadowCascade shadowCascade = shadowCascades[shadowCascadeIndex];

    // Transform from screen coordinates to texture coordinates
    vec4 positionDirLightSpace = posDirLightSpace[shadowCascadeIndex];

    vec3 projCoords = positionDirLightSpace.xyz;// / positionDirLightSpace.w;

    projCoords = projCoords * 0.5 + 0.5;

    if(projCoords.z > 1.0) {
        return 0.0;
    }

    float bias = 0.005; //max(0.05 * (1.0 - dot(fragmentNormal, u_DirectionalLight.direction.xyz)), 0.005);

    float shadow = 0.0;

    float numberOfSamples = 9.0;

    sampler2D depthMap = shadowCascade.depthMap;

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