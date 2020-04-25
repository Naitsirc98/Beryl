package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.graphics.opengl.rendering.shadows.GLShadowsInfo;
import naitsirc98.beryl.graphics.rendering.Renderer;
import naitsirc98.beryl.scenes.Scene;

public class GLMeshRenderer implements Renderer {

    private final GLStaticMeshRenderer staticMeshRenderer;
    private final GLAnimMeshRenderer animMeshRenderer;

    public GLMeshRenderer(GLShadowsInfo shadowsInfo) {
        this.staticMeshRenderer = new GLStaticMeshRenderer(shadowsInfo);
        this.animMeshRenderer = new GLAnimMeshRenderer(shadowsInfo);
    }

    @Override
    public void init() {
        staticMeshRenderer.init();
        animMeshRenderer.init();
    }

    public void prepare(Scene scene) {
        staticMeshRenderer.prepare(scene);
        animMeshRenderer.prepare(scene);
    }

    public void preComputeFrustumCulling(Scene scene) {
        staticMeshRenderer.preComputeFrustumCulling(scene);
        animMeshRenderer.preComputeFrustumCulling(scene);
    }

    @Override
    public void render(Scene scene) {
        staticMeshRenderer.render(scene);
        animMeshRenderer.render(scene);
    }

    public void renderPreComputedVisibleObjects(Scene scene) {
        staticMeshRenderer.renderPreComputedVisibleObjects(scene);
        animMeshRenderer.renderPreComputedVisibleObjects(scene);
    }

    public GLStaticMeshRenderer staticMeshRenderer() {
        return staticMeshRenderer;
    }

    public GLAnimMeshRenderer animMeshRenderer() {
        return animMeshRenderer;
    }

    @Override
    public void terminate() {
        staticMeshRenderer.terminate();
        animMeshRenderer.terminate();
    }
}
