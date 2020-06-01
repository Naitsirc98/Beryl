package naitsirc98.beryl.graphics.opengl.rendering.shadows;

import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;

import static naitsirc98.beryl.util.Maths.roundUp2;
import static naitsirc98.beryl.util.types.DataType.*;

/*
struct ShadowCascade {
    mat4 lightMatrix;
    layout(bindless_sampler) sampler2D shadowMap;
    float farPlane;
    float _padding;
};

layout(std140, binding = 5) uniform ShadowsInfo {
    ShadowCascade u_ShadowCascades[MAX_SHADOW_CASCADES_COUNT];
    bool u_ShadowsEnabled;
};
* */
public interface GLShadowsInfo {

    int MAX_SHADOW_CASCADES_COUNT = 3;

    int SHADOW_CASCADE_SIZEOF = MATRIX4_SIZEOF + SAMPLER_SIZEOF + 2 * FLOAT32_SIZEOF;

    int SHADOW_CASCADES_ARRAY_OFFSET = 0;
    int SHADOWS_ENABLED_OFFSET = MAX_SHADOW_CASCADES_COUNT * SHADOW_CASCADE_SIZEOF;
    int SHADOWS_BUFFER_SIZE = SHADOWS_ENABLED_OFFSET + INT32_SIZEOF;


    GLBuffer buffer();

    GLTexture2D[] dirShadowMaps();

}
