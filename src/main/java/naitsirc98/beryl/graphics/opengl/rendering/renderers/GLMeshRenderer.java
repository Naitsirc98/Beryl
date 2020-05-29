package naitsirc98.beryl.graphics.opengl.rendering.renderers;

import naitsirc98.beryl.graphics.opengl.GLContext;
import naitsirc98.beryl.graphics.opengl.rendering.GLShadingPipeline;
import naitsirc98.beryl.graphics.opengl.rendering.shadows.GLShadowsInfo;
import naitsirc98.beryl.graphics.rendering.Renderer;
import naitsirc98.beryl.scenes.Scene;

public class GLMeshRenderer extends GLRenderer {

    private final GLStaticMeshRenderer staticMeshRenderer;

    public GLMeshRenderer(GLContext context, GLShadowsInfo shadowsInfo) {
        super(context);
        this.staticMeshRenderer = new GLStaticMeshRenderer(context, shadowsInfo);
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

    public void render(Scene scene, GLShadingPipeline shadingPipeline) {
        staticMeshRenderer.render(scene, shadingPipeline);
    }

    public void renderPreComputedVisibleObjects(Scene scene, GLShadingPipeline shadingPipeline) {
        staticMeshRenderer.renderPreComputedVisibleObjects(scene, shadingPipeline);
    }

    public GLStaticMeshRenderer staticMeshRenderer() {
        return staticMeshRenderer;
    }

    @Override
    public void terminate() {
        staticMeshRenderer.terminate();
    }
}
