package naitsirc98.beryl.meshes;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.buffers.IndexBuffer;
import naitsirc98.beryl.graphics.buffers.VertexBuffer;

import java.util.ArrayList;
import java.util.List;

public abstract class MeshStorageHandler<T extends Mesh> {

    protected static final int VERTEX_BUFFER_INITIAL_CAPACITY = 1024 * 1024; // 1MB
    protected static final int INDEX_BUFFER_INITIAL_CAPACITY = VERTEX_BUFFER_INITIAL_CAPACITY * 3; // 3MB

    // All meshes handled by this MeshStorageHandler
    private final List<T> meshes;
    // Buffer for drawing
    private final VertexBuffer vertexBuffer;
    private final IndexBuffer indexBuffer;
    // Offsets and indices
    private long vertexBufferOffset;
    private long indexBufferOffset;
    private int firstIndex;
    private int baseVertex;

    protected MeshStorageHandler() {
        meshes = new ArrayList<>();
        vertexBuffer = createVertexBuffer();
        indexBuffer = createIndexBuffer();
    }

    public int count() {
        return meshes.size();
    }

    public T mesh(int index) {
        return meshes.get(index);
    }

    protected synchronized void allocate(T mesh) {

        checkBuffers(mesh);

        copyVertexData(mesh);
        copyIndexData(mesh);

        mesh.storageInfo().index(meshes.size());

        meshes.add(mesh);
    }

    protected synchronized void free(int index) {
        free(mesh(index));
    }

    protected synchronized void free(T mesh) {

        // Free the storage occupied by the given mesh

        MeshStorageInfo meshStorageInfo = mesh.storageInfo();

        final int index = meshStorageInfo.index();
        vertexBufferOffset = meshStorageInfo.vertexBufferOffset();
        indexBufferOffset = meshStorageInfo.indexBufferOffset();
        baseVertex = meshStorageInfo.baseVertex();
        firstIndex = meshStorageInfo.firstIndex();

        for(int i = index;i < meshes.size();i++) {

            final T nextMesh = meshes.get(i);

            copyVertexData(nextMesh);
            copyIndexData(nextMesh);

            nextMesh.storageInfo().index(i);
        }

        meshes.remove(index);
    }

    protected void clear() {
        meshes.clear();
        vertexBuffer.reallocate(VERTEX_BUFFER_INITIAL_CAPACITY);
        indexBuffer.reallocate(INDEX_BUFFER_INITIAL_CAPACITY);
        vertexBufferOffset = 0;
        indexBufferOffset = 0;
        firstIndex = 0;
        baseVertex = 0;
    }

    protected void terminate() {
        meshes.clear();
        vertexBuffer.release();
        indexBuffer.release();
    }

    @SuppressWarnings("unchecked")
    public <B extends VertexBuffer> B vertexBuffer() {
        return (B) vertexBuffer;
    }

    @SuppressWarnings("unchecked")
    public <B extends IndexBuffer> B indexBuffer() {
        return (B) indexBuffer;
    }

    public long vertexBufferOffset() {
        return vertexBufferOffset;
    }

    public long indexBufferOffset() {
        return indexBufferOffset;
    }

    protected void copyVertexData(T mesh) {

        vertexBuffer.update(vertexBufferOffset, mesh.vertexData());

        mesh.storageInfo().baseVertex(baseVertex);
        mesh.storageInfo().vertexBufferOffset(vertexBufferOffset);

        baseVertex += mesh.vertexCount();
        vertexBufferOffset += mesh.vertexData().capacity();
    }

    protected void copyIndexData(T mesh) {

        indexBuffer.update(indexBufferOffset, mesh.indexData());

        mesh.storageInfo().firstIndex(firstIndex);
        mesh.storageInfo().indexBufferOffset(indexBufferOffset);

        firstIndex += mesh.indexCount();
        indexBufferOffset += mesh.indexData().capacity();
    }

    protected void checkBuffers(T mesh) {

        final int vertexDataSize = mesh.vertexData().capacity();
        final int indexDataSize = mesh.indexData().capacity();

        if(vertexBufferOffset + vertexDataSize > vertexBuffer.size()) {
            vertexBuffer.resize(vertexBuffer.size() + vertexDataSize);
        }

        if(indexBufferOffset + indexDataSize > indexBuffer.size()) {
            indexBuffer.resize(indexBuffer.size() + indexDataSize);
        }
    }

    protected IndexBuffer createIndexBuffer() {
        IndexBuffer indexBuffer = GraphicsFactory.get().newIndexBuffer();
        indexBuffer.allocate(INDEX_BUFFER_INITIAL_CAPACITY);
        indexBufferOffset = 0;
        return indexBuffer;
    }

    protected VertexBuffer createVertexBuffer() {
        VertexBuffer vertexBuffer = GraphicsFactory.get().newVertexBuffer();
        vertexBuffer.allocate(VERTEX_BUFFER_INITIAL_CAPACITY);
        vertexBufferOffset = 0;
        return vertexBuffer;
    }
}
