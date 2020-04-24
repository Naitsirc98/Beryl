package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.graphics.rendering.renderers.StaticMeshRenderer;
import naitsirc98.beryl.meshes.MeshManager;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.meshes.StaticMeshManager;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import naitsirc98.beryl.meshes.views.StaticMeshView;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.meshes.MeshInstanceList;
import naitsirc98.beryl.scenes.components.meshes.StaticMeshInstance;

import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.meshes.vertices.VertexAttribute.*;

public final class GLStaticMeshRenderer extends GLIndirectRenderer implements StaticMeshRenderer {

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void terminate() {
        super.terminate();
    }

    @Override
    protected void updateVertexArrayVertexBuffer() {

        StaticMeshManager staticMeshManager = MeshManager.get().staticMeshManager();

        GLBuffer vertexBuffer = staticMeshManager.vertexBuffer();
        GLBuffer indexBuffer = staticMeshManager.indexBuffer();

        vertexArray.setVertexBuffer(0, vertexBuffer, StaticMesh.VERTEX_DATA_SIZE);
        vertexArray.setIndexBuffer(indexBuffer);
    }

    @Override
    public MeshInstanceList<StaticMeshInstance> getInstances(Scene scene) {
        return scene.meshInfo().meshViewsOfType(StaticMeshView.class);
    }

    @Override
    protected void initVertexArray() {

        vertexArray = new GLVertexArray();

        VertexLayout vertexLayout = new VertexLayout.Builder(2)
                .put(0, 0, POSITION3D, NORMAL, TEXCOORDS2D)
                .put(1, 3, INDEX, INDEX).instanced(1, true)
                .build();

        for (int i = 0; i < vertexLayout.bindings(); i++) {
            vertexArray.setVertexAttributes(i, vertexLayout.attributeList(i));
        }

        instanceBuffer = new GLBuffer("INSTANCE_VERTEX_BUFFER");
    }

    @Override
    protected void initRenderShader() {
        renderShader = new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(BerylFiles.getPath("shaders/phong/phong_indirect.vert")))
                .attach(new GLShader(FRAGMENT_STAGE).source(BerylFiles.getPath("shaders/phong/phong_indirect.frag")))
                .link();
    }
}
