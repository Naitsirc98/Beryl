package naitsirc98.beryl.graphics.opengl.rendering.renderers.data;

import naitsirc98.beryl.graphics.opengl.GLContext;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.meshes.MeshManager;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.meshes.vertices.VertexLayout;

import static naitsirc98.beryl.meshes.vertices.VertexLayouts.VERTEX_LAYOUT_3D_INDIRECT;

public class GLStaticRenderData extends GLRenderData {

    public GLStaticRenderData(GLContext context) {
        super(context);
    }

    @Override
    protected GLBuffer initVertexBuffer() {
        return MeshManager.get().storageHandler(StaticMesh.class).vertexBuffer();
    }

    @Override
    protected GLBuffer initIndexBuffer() {
        return MeshManager.get().storageHandler(StaticMesh.class).indexBuffer();
    }

    @Override
    protected int getStride() {
        return StaticMesh.VERTEX_DATA_SIZE;
    }

    @Override
    protected GLVertexArray initVertexArray() {

        GLVertexArray vertexArray = new GLVertexArray(context());

        VertexLayout vertexLayout = VERTEX_LAYOUT_3D_INDIRECT;

        for (int i = 0; i < vertexLayout.bindings(); i++) {
            vertexArray.setVertexAttributes(i, vertexLayout.attributeList(i));
        }

        return vertexArray;
    }

    @Override
    protected GLBuffer initInstanceBuffer() {
        return new GLBuffer(context()).name("INSTANCE VERTEX BUFFER");
    }
}
