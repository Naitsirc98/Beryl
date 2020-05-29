package naitsirc98.beryl.graphics.opengl.rendering.shadows;

import naitsirc98.beryl.graphics.opengl.GLContext;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.rendering.renderers.GLMeshRenderer;
import naitsirc98.beryl.graphics.opengl.rendering.renderers.GLRenderer;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.rendering.shadows.ShadowCascade;
import naitsirc98.beryl.scenes.Scene;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static naitsirc98.beryl.util.types.DataType.MATRIX4_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.VECTOR4_SIZEOF;

public class GLShadowRenderer extends GLRenderer implements GLShadowsInfo {

    private GLDirectionalShadowRenderer directionalShadowRenderer;
    private GLBuffer shadowsBuffer;

    public GLShadowRenderer(GLContext context) {
        super(context);
    }

    @Override
    public void init() {
        directionalShadowRenderer = new GLDirectionalShadowRenderer(context());
        shadowsBuffer = new GLBuffer(context()).name("SHADOWS BUFFER");
        shadowsBuffer.allocate(SHADOWS_BUFFER_SIZE);
        shadowsBuffer.mapMemory();
    }

    public void render(Scene scene, GLMeshRenderer meshRenderer) {
        directionalShadowRenderer.bakeDirectionalShadows(scene, meshRenderer);
        updateShadowsBuffer();
    }

    @Override
    public void terminate() {
        directionalShadowRenderer.terminate();
        shadowsBuffer.release();
    }

    @Override
    public GLBuffer buffer() {
        return shadowsBuffer;
    }

    @Override
    public GLTexture2D[] dirShadowMaps() {
        return directionalShadowRenderer.dirShadowMaps();
    }

    private void updateShadowsBuffer() {

        // CHECK ALIGNMENT

        try(MemoryStack stack = MemoryStack.stackPush()) {

            ByteBuffer shadowInfo = stack.calloc(SHADOWS_BUFFER_SIZE);

            getDirLightMatrices(shadowInfo);
            getCascadeFarPlanes(shadowInfo);

            shadowsBuffer.copy(0, shadowInfo);
        }
    }

    private void getDirLightMatrices(ByteBuffer shadowInfo) {

        ShadowCascade[] shadowCascades = directionalShadowRenderer.shadowCascades();

        for(int i = 0;i < MAX_SHADOW_CASCADES_COUNT;i++) {

            Matrix4fc cascadeProjectionViewMatrix = shadowCascades[i].lightProjectionViewMatrix();

            cascadeProjectionViewMatrix.get(SHADOWS_BUFFER_DIR_MATRICES_OFFSET + i * MATRIX4_SIZEOF, shadowInfo);
        }
    }

    private void getCascadeFarPlanes(ByteBuffer shadowInfo) {

        ShadowCascade[] shadowCascades = directionalShadowRenderer.shadowCascades();

        for(int i = 0;i < MAX_SHADOW_CASCADES_COUNT;i++) {

            final float farPlane = shadowCascades[i].farPlane();

            shadowInfo.putFloat(SHADOWS_BUFFER_CASCADE_FAR_PLANES_OFFSET + i * VECTOR4_SIZEOF, farPlane);
        }
    }
}
