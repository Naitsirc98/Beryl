package naitsirc98.beryl.graphics.opengl.rendering.renderers.data;

import naitsirc98.beryl.graphics.buffers.MappedGraphicsBuffer;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.commands.GLDrawElementsCommand;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.resources.Resource;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.meshes.MeshInstanceList;

import static naitsirc98.beryl.util.types.DataType.INT32_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.MATRIX4_SIZEOF;

public abstract class GLRenderData implements Resource {

    protected static final int VERTEX_BUFFER_BINDING = 0;
    protected static final int INSTANCE_BUFFER_BINDING = 1;

    private static final int INSTANCE_BUFFER_MIN_SIZE = INT32_SIZEOF * 2;
    private static final int TRANSFORMS_BUFFER_MIN_SIZE = MATRIX4_SIZEOF * 2;


    private GLVertexArray vertexArray;

    private GLBuffer instanceBuffer; // model matrix + material
    private GLBuffer transformsBuffer;
    private GLBuffer commandBuffer;
    private GLBuffer meshIndicesBuffer;
    private GLBuffer vertexBuffer;
    private GLBuffer indexBuffer;

    public GLRenderData() {
        transformsBuffer = new GLBuffer("TRANSFORMS_STORAGE_BUFFER");
        commandBuffer = new GLBuffer("INSTANCE_COMMAND_BUFFER");
        meshIndicesBuffer = new GLBuffer("MESH_INDICES_STORAGE_BUFFER");
        vertexArray = initVertexArray();
        instanceBuffer = initInstanceBuffer();
        vertexBuffer = initVertexBuffer();
        indexBuffer = initIndexBuffer();
    }

    @Override
    public void release() {
        vertexArray.release();
        instanceBuffer.release();
        transformsBuffer.release();
        commandBuffer.release();
        meshIndicesBuffer.release();
    }

    public GLVertexArray getVertexArray() {
        return vertexArray;
    }

    public GLBuffer getInstanceBuffer() {
        return instanceBuffer;
    }

    public GLBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public GLBuffer getIndexBuffer() {
        return indexBuffer;
    }

    public GLBuffer getTransformsBuffer() {
        return transformsBuffer;
    }

    public GLBuffer getCommandBuffer() {
        return commandBuffer;
    }

    public GLBuffer getMeshIndicesBuffer() {
        return meshIndicesBuffer;
    }

    public void update(Scene scene, MeshInstanceList<?> instances) {
        updateVertexArrayVertexBuffer();
        prepareInstanceBuffer(scene, instances);
    }

    protected void updateVertexArrayVertexBuffer() {
        final int stride = getStride();
        vertexArray.setVertexBuffer(VERTEX_BUFFER_BINDING, vertexBuffer, stride);
        vertexArray.setIndexBuffer(indexBuffer);
    }

    protected abstract GLBuffer initInstanceBuffer();

    protected abstract GLBuffer initVertexBuffer();

    protected abstract GLBuffer initIndexBuffer();

    protected abstract int getStride();

    protected abstract GLVertexArray initVertexArray();

    protected boolean prepareInstanceBuffer(Scene scene, MeshInstanceList<?> instances) {

        final int numObjects = instances == null ? 0 : instances.numMeshViews();

        if(numObjects == 0) {
            return false;
        }

        checkCommandBuffer(numObjects);

        checkPerInstanceDataBuffer(numObjects);

        checkTransformsBuffer(numObjects);

        return true;
    }

    private void checkTransformsBuffer(int numObjects) {

        final int transformsMinSize = numObjects * TRANSFORMS_BUFFER_MIN_SIZE;

        if (transformsBuffer.size() < transformsMinSize) {
            reallocateBuffer(transformsBuffer, transformsMinSize);
        }
    }

    private void checkPerInstanceDataBuffer(int numObjects) {

        final int instancesMinSize = numObjects * INSTANCE_BUFFER_MIN_SIZE;

        if (instanceBuffer.size() < instancesMinSize) {
            reallocateBuffer(instanceBuffer, instancesMinSize);
            vertexArray.setVertexBuffer(INSTANCE_BUFFER_BINDING, instanceBuffer, INSTANCE_BUFFER_MIN_SIZE);
        }
    }

    private void checkCommandBuffer(int numObjects) {

        final int commandBufferMinSize = numObjects * GLDrawElementsCommand.SIZEOF;

        if(commandBuffer.size() < commandBufferMinSize) {
            reallocateBuffer(commandBuffer, commandBufferMinSize);
        }
    }

    private void reallocateBuffer(MappedGraphicsBuffer buffer, long size) {

        buffer.unmapMemory();

        buffer.reallocate(size);

        if(!buffer.mapped()) {
            buffer.mapMemory();
        }
    }
}
