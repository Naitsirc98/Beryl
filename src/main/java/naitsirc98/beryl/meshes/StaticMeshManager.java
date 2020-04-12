package naitsirc98.beryl.meshes;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.buffers.IndexBuffer;
import naitsirc98.beryl.graphics.buffers.StorageBuffer;
import naitsirc98.beryl.graphics.buffers.VertexBuffer;
import naitsirc98.beryl.graphics.opengl.commands.GLDrawElementsCommand;
import naitsirc98.beryl.util.geometry.ISphere;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;

public final class StaticMeshManager {

    // TODO: Handle resizing, destruction leaks and concurrent access

    private static final int VERTEX_BUFFER_INITIAL_CAPACITY = 1024 * 1024; // 1MB
    private static final int INDEX_BUFFER_INITIAL_CAPACITY = 1024 * 1024; // 1MB
    private static final int BOUNDING_SPHERES_BUFFER_INITIAL_CAPACITY = 100 * ISphere.SIZEOF;
    private static final int COMMAND_BUFFER_INITIAL_CAPACITY = 100 * GLDrawElementsCommand.SIZEOF;

    private final VertexBuffer vertexBuffer;
    private final IndexBuffer indexBuffer;
    private final StorageBuffer boundingSpheresBuffer;
    private final StorageBuffer commandBuffer;
    private long vertexBufferOffset;
    private long indexBufferOffset;
    private long boundingSpheresBufferOffset;
    private long commandBufferOffset;
    private int firstIndex;
    private int baseVertex;
    private int count;

    StaticMeshManager() {
        vertexBuffer = GraphicsFactory.get().newVertexBuffer();
        vertexBuffer.allocate(VERTEX_BUFFER_INITIAL_CAPACITY);
        vertexBufferOffset = 0;

        indexBuffer = GraphicsFactory.get().newIndexBuffer();
        indexBuffer.allocate(INDEX_BUFFER_INITIAL_CAPACITY);
        indexBufferOffset = 0;

        boundingSpheresBuffer = GraphicsFactory.get().newStorageBuffer();
        boundingSpheresBuffer.allocate(BOUNDING_SPHERES_BUFFER_INITIAL_CAPACITY);
        boundingSpheresBufferOffset = 0;

        commandBuffer = GraphicsFactory.get().newStorageBuffer();
        commandBuffer.allocate(COMMAND_BUFFER_INITIAL_CAPACITY);
        commandBufferOffset = 0;
        firstIndex = 0;
        baseVertex = 0;
    }

    StaticMesh create(int handle, String name, ByteBuffer vertices, ByteBuffer indices) {

        StaticMesh mesh = new StaticMesh(handle, name, vertices, indices);

        copyVertexData(mesh);
        copyIndexData(mesh);
        copyBoundingSphereData(mesh);
        copyCommandInfo(mesh);

        mesh.setIndex(count++);

        return mesh;
    }

    int count() {
        return count;
    }

    void destroy(StaticMesh mesh) {

        // TODO: manage memory leaks

        --count;
    }

    void clear() {
        vertexBufferOffset = 0;
        indexBufferOffset = 0;
        boundingSpheresBufferOffset = 0;
        commandBufferOffset = 0;
        firstIndex = 0;
        baseVertex = 0;
        count = 0;
    }

    void terminate() {
        vertexBuffer.release();
        indexBuffer.release();
        boundingSpheresBuffer.release();
        commandBuffer.release();
    }

    public VertexBuffer vertexBuffer() {
        return vertexBuffer;
    }

    public IndexBuffer indexBuffer() {
        return indexBuffer;
    }

    public StorageBuffer boundingSpheresBuffer() {
        return boundingSpheresBuffer;
    }

    public StorageBuffer commandBuffer() {
        return commandBuffer;
    }

    public long vertexBufferOffset() {
        return vertexBufferOffset;
    }

    public long indexBufferOffset() {
        return indexBufferOffset;
    }

    public long boundingSpheresBufferOffset() {
        return boundingSpheresBufferOffset;
    }

    public long commandBufferOffset() {
        return commandBufferOffset;
    }

    private void copyVertexData(StaticMesh mesh) {

        vertexBuffer.update(vertexBufferOffset, mesh.vertexData());

        mesh.setVertexBufferOffset(vertexBufferOffset);

        vertexBufferOffset += mesh.vertexData().capacity();
    }

    private void copyIndexData(StaticMesh mesh) {

        indexBuffer.update(indexBufferOffset, mesh.indexData());

        mesh.setIndexBufferOffset(indexBufferOffset);

        indexBufferOffset += mesh.indexData().capacity();
    }

    private void copyBoundingSphereData(StaticMesh mesh) {

        try(MemoryStack stack = stackPush()) {

            ISphere sphere = mesh.boundingSphere();

            boundingSpheresBuffer.update(boundingSpheresBufferOffset, sphere.get(0, stack.calloc(ISphere.SIZEOF)));

            mesh.setBoundingSphereOffset(boundingSpheresBufferOffset);

            boundingSpheresBufferOffset += ISphere.SIZEOF;
        }
    }

    private void copyCommandInfo(StaticMesh mesh) {

        try(MemoryStack stack = stackPush()) {

            GLDrawElementsCommand command = GLDrawElementsCommand.callocStack(stack);

            command.count(mesh.indexCount())
                    .firstIndex(firstIndex)
                    .baseVertex(baseVertex);

            firstIndex += mesh.indexCount();
            baseVertex += mesh.vertexCount();

            commandBuffer.update(commandBufferOffset, command.buffer());

            commandBufferOffset += GLDrawElementsCommand.SIZEOF;
        }

    }
}
