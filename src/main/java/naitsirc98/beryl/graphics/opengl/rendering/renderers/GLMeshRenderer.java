package naitsirc98.beryl.graphics.opengl.rendering.renderers;

import naitsirc98.beryl.graphics.opengl.rendering.shadows.GLShadowsInfo;
import naitsirc98.beryl.graphics.rendering.Renderer;
import naitsirc98.beryl.scenes.Scene;

public class GLMeshRenderer implements Renderer {

    private final GLStaticMeshRenderer staticMeshRenderer;

    public GLMeshRenderer(GLShadowsInfo shadowsInfo) {
        this.staticMeshRenderer = new GLStaticMeshRenderer(shadowsInfo);
    }

    @Override
    public void init() {
        staticMeshRenderer.init();
    }

    public void prepare(Scene scene) {
        staticMeshRenderer.prepare(scene);
    }

    public void preComputeFrustumCulling(Scene scene) {
        staticMeshRenderer.preComputeFrustumCulling(scene);
    }

    public void render(Scene scene, boolean shadowsEnabled) {
        staticMeshRenderer.render(scene, shadowsEnabled);
    }

    public void renderPreComputedVisibleObjects(Scene scene, boolean shadowsEnabled) {
        staticMeshRenderer.renderPreComputedVisibleObjects(scene, shadowsEnabled);
    }

    public GLStaticMeshRenderer staticMeshRenderer() {
        return staticMeshRenderer;
    }

    @Override
    public void terminate() {
        staticMeshRenderer.terminate();
    }
}
