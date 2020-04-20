package naitsirc98.beryl.meshes;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.buffers.IndexBuffer;
import naitsirc98.beryl.graphics.buffers.StorageBuffer;
import naitsirc98.beryl.graphics.buffers.VertexBuffer;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.commands.GLDrawElementsCommand;
import naitsirc98.beryl.util.geometry.ISphere;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.system.MemoryStack.stackPush;

public final class AnimMeshManager {

    // TODO: Handle resizing, destruction leaks and concurrent access

    private static final int VERTEX_BUFFER_INITIAL_CAPACITY = 1024 * 1024; // 1MB
    private static final int INDEX_BUFFER_INITIAL_CAPACITY = 2048 * 1024; // 2MB
    private static final int BOUNDING_SPHERES_BUFFER_INITIAL_CAPACITY = 10 * ISphere.SIZEOF;
    private static final int BONES_ID_INITIAL_CAPACITY = 10 * Bone.SIZEOF;
    private static final int COMMAND_BUFFER_INITIAL_CAPACITY = 10 * GLDrawElementsCommand.SIZEOF;

    private final VertexBuffer vertexBuffer;
    private final IndexBuffer indexBuffer;
    private final StorageBuffer boundingSpheresBuffer;
    private final StorageBuffer bonesBuffer;
    private final StorageBuffer commandBuffer;
    private final Map<String, Bone> boneNames;

    private long vertexBufferOffset;
    private long indexBufferOffset;
    private long boundingSpheresBufferOffset;
    private long bonesBufferOffset;
    private long commandBufferOffset;
    private int firstIndex;
    private int baseVertex;
    private int count;

    AnimMeshManager() {

        vertexBuffer = GraphicsFactory.get().newVertexBuffer();
        vertexBuffer.allocate(VERTEX_BUFFER_INITIAL_CAPACITY);
        vertexBufferOffset = 0;

        indexBuffer = GraphicsFactory.get().newIndexBuffer();
        indexBuffer.allocate(INDEX_BUFFER_INITIAL_CAPACITY);
        indexBufferOffset = 0;

        boundingSpheresBuffer = GraphicsFactory.get().newStorageBuffer();
        boundingSpheresBuffer.allocate(BOUNDING_SPHERES_BUFFER_INITIAL_CAPACITY);
        boundingSpheresBufferOffset = 0;

        bonesBuffer = GraphicsFactory.get().newStorageBuffer();
        bonesBuffer.allocate(BONES_ID_INITIAL_CAPACITY);
        bonesBufferOffset = 0;

        boneNames = new HashMap<>();

        commandBuffer = GraphicsFactory.get().newStorageBuffer();
        commandBuffer.allocate(COMMAND_BUFFER_INITIAL_CAPACITY);
        commandBufferOffset = 0;
        firstIndex = 0;
        baseVertex = 0;
    }

    void setAnimMeshInfo(AnimMesh mesh) {

        checkBuffers(mesh);

        copyVertexData(mesh);
        copyIndexData(mesh);
        copyBoundingSphereData(mesh);
        copyCommandInfo(mesh);

        mesh.setIndex(count++);
    }

    public Bone bone(String name) {
        return boneNames.get(name);
    }

    public int setBoneData(Bone bone) {

        if(bonesBufferOffset >= bonesBuffer.size()) {
            bonesBuffer.resize(bonesBuffer.size() * 2);
        }

        final int boneID = (int) (bonesBufferOffset / Bone.SIZEOF);

        try(MemoryStack stack = stackPush()) {

            ByteBuffer data = stack.malloc(Bone.SIZEOF);

            bone.transformation().get(data);

            bonesBuffer.update(bonesBufferOffset, data);

            bonesBufferOffset += Bone.SIZEOF;
        }

        boneNames.put(bone.name(), bone);

        return boneID;
    }

    int count() {
        return count;
    }

    void destroy(AnimMesh mesh) {

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

    @SuppressWarnings("unchecked")
    public <T extends VertexBuffer> T vertexBuffer() {
        return (T) vertexBuffer;
    }

    @SuppressWarnings("unchecked")
    public <T extends IndexBuffer> T indexBuffer() {
        return (T) indexBuffer;
    }

    @SuppressWarnings("unchecked")
    public <T extends StorageBuffer> T boundingSpheresBuffer() {
        return (T) boundingSpheresBuffer;
    }

    @SuppressWarnings("unchecked")
    public <T extends StorageBuffer> T commandBuffer() {
        return (T) commandBuffer;
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

    public <T extends StorageBuffer> T bonesBuffer() {
        return (T) bonesBuffer;
    }

    private void copyVertexData(AnimMesh mesh) {

        vertexBuffer.update(vertexBufferOffset, mesh.vertexData());

        mesh.setVertexBufferOffset(vertexBufferOffset);

        vertexBufferOffset += mesh.vertexData().capacity();
    }

    private void copyIndexData(AnimMesh mesh) {

        indexBuffer.update(indexBufferOffset, mesh.indexData());

        mesh.setIndexBufferOffset(indexBufferOffset);

        indexBufferOffset += mesh.indexData().capacity();
    }

    private void copyBoundingSphereData(AnimMesh mesh) {

        try(MemoryStack stack = stackPush()) {

            ISphere sphere = mesh.boundingSphere();

            boundingSpheresBuffer.update(boundingSpheresBufferOffset, sphere.get(0, stack.calloc(ISphere.SIZEOF)));

            mesh.setBoundingSphereOffset(boundingSpheresBufferOffset);

            boundingSpheresBufferOffset += ISphere.SIZEOF;
        }
    }

    private void copyCommandInfo(AnimMesh mesh) {

        try(MemoryStack stack = stackPush()) {

            GLDrawElementsCommand command = GLDrawElementsCommand.callocStack(stack);

            command.count(mesh.indexCount())
                    .firstIndex(firstIndex)
                    .baseVertex(baseVertex);

            commandBuffer.update(commandBufferOffset, command.buffer());

            commandBufferOffset += GLDrawElementsCommand.SIZEOF;
            mesh.setFirstIndex(firstIndex);
            mesh.setBaseVertex(baseVertex);
            firstIndex += mesh.indexCount();
            baseVertex += mesh.vertexCount();
        }

    }

    private void checkBuffers(Mesh mesh) {

        final int vertexDataSize = mesh.vertexData().capacity();
        final int indexDataSize = mesh.indexData().capacity();

        if(vertexBufferOffset + vertexDataSize > vertexBuffer.size()) {
            vertexBuffer.resize(vertexBuffer.size() + vertexDataSize);
        }

        if(indexBufferOffset + indexDataSize > indexBuffer.size()) {
            indexBuffer.resize(indexBuffer.size() + indexDataSize);
        }

        if(boundingSpheresBufferOffset + ISphere.SIZEOF > boundingSpheresBuffer.size()) {
            boundingSpheresBuffer.resize(boundingSpheresBuffer.size() + 4 * ISphere.SIZEOF);
        }

        if(commandBufferOffset + GLDrawElementsCommand.SIZEOF > commandBuffer.size()) {
            commandBuffer.resize(commandBuffer.size() + 4 * GLDrawElementsCommand.SIZEOF);
        }
    }

}
