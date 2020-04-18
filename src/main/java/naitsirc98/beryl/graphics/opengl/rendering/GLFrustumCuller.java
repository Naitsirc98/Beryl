package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.commands.GLDrawElementsCommand;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.meshes.views.MeshView;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.meshes.MeshInstance;
import naitsirc98.beryl.scenes.components.meshes.MeshInstanceList;
import naitsirc98.beryl.util.SystemInfo;
import naitsirc98.beryl.util.geometry.ISphere;
import org.joml.FrustumIntersection;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.min;
import static java.util.stream.IntStream.range;
import static naitsirc98.beryl.util.types.DataType.INT32_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.MATRIX4_SIZEOF;
import static org.lwjgl.system.MemoryStack.stackPush;

public class GLFrustumCuller {

    private static final int THREAD_COUNT = SystemInfo.processorCount();

    private static final int INSTANCE_BUFFER_MIN_SIZE = INT32_SIZEOF * 2;

    private static final int TRANSFORMS_BUFFER_MIN_SIZE = MATRIX4_SIZEOF * 2;
    private static final int TRANSFORMS_BUFFER_MODEL_MATRIX_OFFSET = 0;
    private static final int TRANSFORMS_BUFFER_NORMAL_MATRIX_OFFSET = MATRIX4_SIZEOF;

    private final ExecutorService executor;
    private final AtomicInteger baseInstance;
    private final GLBuffer commandBuffer;
    private final GLBuffer transformsBuffer;
    private final GLBuffer instanceBuffer;

    public GLFrustumCuller(GLBuffer commandBuffer, GLBuffer transformsBuffer, GLBuffer instanceBuffer) {
        this.commandBuffer = commandBuffer;
        this.transformsBuffer = transformsBuffer;
        this.instanceBuffer = instanceBuffer;
        this.executor = Executors.newFixedThreadPool(THREAD_COUNT);
        baseInstance = new AtomicInteger();
    }

    public void terminate() {
        executor.shutdown();
        try {
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.error("Timeout error", e);
        }
    }

    public int performCullingCPU(Scene scene, MeshInstanceList<?> instances, boolean alwaysPass) {

        final int batchSize = (int) Math.ceil((float) instances.size() / (float) THREAD_COUNT);

        final GLBuffer commandBuffer = this.commandBuffer;

        final FrustumIntersection frustum = scene.camera().frustum();

        final CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT);

        range(0, THREAD_COUNT).parallel().unordered().forEach(i -> {

            final int batchBegin = i * batchSize;
            final int batchEnd = min(batchBegin + batchSize, instances.size());

            executor.submit(() -> {

                try(MemoryStack stack = stackPush()) {

                    Vector4f center = new Vector4f();

                    GLDrawElementsCommand command = GLDrawElementsCommand.callocStack(stack);

                    for(int j = batchBegin;j < batchEnd;j++) {

                        MeshInstance<?> instance = instances.get(j);

                        final Vector3fc scale = instance.transform().scale();
                        final float maxScale = scale.get(scale.maxComponent());
                        final Matrix4fc modelMatrix = instance.modelMatrix();

                        for (MeshView<?> meshView : instance) {

                            final Mesh mesh = meshView.mesh();
                            final ISphere sphere = mesh.boundingSphere();

                            center.set(sphere.center(), 1.0f).mul(modelMatrix);

                            final float radius = sphere.radius() * maxScale;

                            if(alwaysPass || frustum.testSphere(center.x, center.y, center.z, radius)) {

                                final int instanceID = baseInstance.getAndIncrement();

                                setInstanceTransform(instanceID, instance.modelMatrix(), instance.normalMatrix());

                                final int materialIndex = meshView.material().bufferIndex();

                                setInstanceData(instanceID, instanceID, materialIndex);

                                command.count(mesh.indexCount())
                                        .primCount(1)
                                        .firstIndex(mesh.firstIndex())
                                        .baseVertex(mesh.baseVertex())
                                        .baseInstance(instanceID);

                                commandBuffer.copy(instanceID * GLDrawElementsCommand.SIZEOF, command.buffer());
                            }
                        }
                    }
                }

                countDownLatch.countDown();
            });
        });

        try {
            countDownLatch.await(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.error("Timeout error", e);
        }

        return baseInstance.getAndSet(0);
    }

    protected void setInstanceTransform(int objectIndex, Matrix4fc modelMatrix, Matrix4fc normalMatrix) {
        try(MemoryStack stack = stackPush()) {
            ByteBuffer buffer = stack.malloc(MATRIX4_SIZEOF * 2);
            modelMatrix.get(TRANSFORMS_BUFFER_MODEL_MATRIX_OFFSET, buffer);
            normalMatrix.get(TRANSFORMS_BUFFER_NORMAL_MATRIX_OFFSET, buffer);
            transformsBuffer.copy(objectIndex * TRANSFORMS_BUFFER_MIN_SIZE, buffer);
        }
    }

    protected void setInstanceData(int instanceID, int matrixIndex, int materialIndex) {

        try (MemoryStack stack = stackPush()) {

            IntBuffer buffer = stack.mallocInt(2);

            buffer.put(0, matrixIndex).put(1, materialIndex);

            instanceBuffer.copy(instanceID * INSTANCE_BUFFER_MIN_SIZE, buffer);
        }
    }

}
