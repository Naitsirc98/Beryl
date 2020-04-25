package naitsirc98.beryl.graphics.opengl.rendering.shadows;

import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;

public interface GLShadowsInfo {

    int MAX_SHADOW_CASCADES_COUNT = 3;

    int SHADOWS_BUFFER_DIR_MATRICES_OFFSET = 0;
    int SHADOWS_BUFFER_CASCADE_FAR_PLANES_OFFSET = 192;
    int SHADOWS_BUFFER_SIZE = 240;

    GLBuffer buffer();

    GLTexture2D[] dirShadowMaps();

}
