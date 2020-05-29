package naitsirc98.beryl.graphics.opengl.rendering.renderers;

import naitsirc98.beryl.graphics.opengl.GLContext;
import naitsirc98.beryl.graphics.opengl.rendering.renderers.data.GLRenderData;
import naitsirc98.beryl.graphics.opengl.rendering.renderers.data.GLStaticRenderData;
import naitsirc98.beryl.graphics.opengl.rendering.shadows.GLShadowsInfo;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.meshes.MeshInstanceList;
import naitsirc98.beryl.scenes.components.meshes.StaticMeshInstance;

public final class GLStaticMeshRenderer extends GLIndirectRenderer {

    public GLStaticMeshRenderer(GLContext context, GLShadowsInfo shadowsInfo) {
        super(context, shadowsInfo);
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    protected GLRenderData createRenderData() {
        return new GLStaticRenderData(context());
    }

    @Override
    public void terminate() {
        super.terminate();
    }

    @Override
    public MeshInstanceList<StaticMeshInstance> getInstances(Scene scene) {
        return scene.meshInfo().getStaticMeshInstances();
    }
}
