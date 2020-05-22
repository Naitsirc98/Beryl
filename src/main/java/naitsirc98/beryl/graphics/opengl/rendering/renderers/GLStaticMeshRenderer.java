package naitsirc98.beryl.graphics.opengl.rendering.renderers;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.rendering.renderers.data.GLRenderData;
import naitsirc98.beryl.graphics.opengl.rendering.renderers.data.GLStaticRenderData;
import naitsirc98.beryl.graphics.opengl.rendering.shadows.GLShadowsInfo;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.graphics.rendering.Renderer;
import naitsirc98.beryl.meshes.MeshManager;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.meshes.MeshInstanceList;
import naitsirc98.beryl.scenes.components.meshes.StaticMeshInstance;

import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.meshes.vertices.VertexLayouts.VERTEX_LAYOUT_3D_INDIRECT;

public final class GLStaticMeshRenderer extends GLIndirectRenderer implements Renderer {

    public GLStaticMeshRenderer(GLShadowsInfo shadowsInfo) {
        super(shadowsInfo);
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    protected GLRenderData createRenderData() {
        return new GLStaticRenderData();
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
