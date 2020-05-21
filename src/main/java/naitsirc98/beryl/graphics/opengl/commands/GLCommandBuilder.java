package naitsirc98.beryl.graphics.opengl.commands;

import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.materials.ManagedMaterial;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.meshes.views.MeshView;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import static naitsirc98.beryl.util.types.DataType.INT32_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.MATRIX4_SIZEOF;
import static org.lwjgl.system.MemoryStack.stackPush;

public class GLCommandBuilder {

    private static final int INSTANCE_BUFFER_MIN_SIZE = INT32_SIZEOF * 2;

    private static final int TRANSFORMS_BUFFER_MIN_SIZE = MATRIX4_SIZEOF * 2;
    private static final int TRANSFORMS_BUFFER_MODEL_MATRIX_OFFSET = 0;
    private static final int TRANSFORMS_BUFFER_NORMAL_MATRIX_OFFSET = MATRIX4_SIZEOF;


    private final AtomicInteger baseInstance;
    private final GLBuffer commandBuffer;
    private final GLBuffer transformsBuffer;
    private final GLBuffer instanceBuffer;

    public GLCommandBuilder(GLBuffer commandBuffer, GLBuffer transformsBuffer, GLBuffer instanceBuffer) {
        this.baseInstance = new AtomicInteger();
        this.commandBuffer = commandBuffer;
        this.transformsBuffer = transformsBuffer;
        this.instanceBuffer = instanceBuffer;
    }

    public int count() {
        return baseInstance.getAndSet(0);
    }

    public void buildDrawCommand(GLDrawElementsCommand command, int matricesIndex, MeshView<?> meshView, Mesh mesh) {

        final int baseInstance = this.baseInstance.getAndIncrement();

        ManagedMaterial material = (ManagedMaterial) meshView.material();

        final int materialIndex = material.storageInfo().bufferIndex();

        setInstanceData(baseInstance, matricesIndex, materialIndex);

        command.count(mesh.indexCount())
                .primCount(1)
                .firstIndex(mesh.storageInfo().firstIndex())
                .baseVertex(mesh.storageInfo().baseVertex())
                .baseInstance(baseInstance);

        commandBuffer.copy(baseInstance * GLDrawElementsCommand.SIZEOF, command.buffer());
    }

    public void setInstanceTransform(int objectIndex, Matrix4fc modelMatrix, Matrix4fc normalMatrix) {
        try(MemoryStack stack = stackPush()) {
            ByteBuffer buffer = stack.malloc(MATRIX4_SIZEOF * 2);
            modelMatrix.get(TRANSFORMS_BUFFER_MODEL_MATRIX_OFFSET, buffer);
            normalMatrix.get(TRANSFORMS_BUFFER_NORMAL_MATRIX_OFFSET, buffer);
            transformsBuffer.copy(objectIndex * TRANSFORMS_BUFFER_MIN_SIZE, buffer);
        }
    }

    public void setInstanceData(int instanceID, int matrixIndex, int materialIndex) {

        try (MemoryStack stack = stackPush()) {

            IntBuffer buffer = stack.mallocInt(2);

            buffer.put(0, matrixIndex).put(1, materialIndex);

            instanceBuffer.copy(instanceID * INSTANCE_BUFFER_MIN_SIZE, buffer);
        }
    }

}
