package naitsirc98.beryl.graphics.opengl.rendering.culling;

import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.commands.GLDrawElementsCommand;
import naitsirc98.beryl.graphics.rendering.culling.FrustumCuller;
import naitsirc98.beryl.graphics.rendering.culling.FrustumCullingPreCondition;
import naitsirc98.beryl.graphics.rendering.culling.FrustumCullingPreConditionState;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.meshes.views.MeshView;
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
import static naitsirc98.beryl.util.types.DataType.INT32_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.MATRIX4_SIZEOF;
import static org.lwjgl.system.MemoryStack.stackPush;

public class GLFrustumCuller implements FrustumCuller {

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

    @Override
    public void init() {

    }

    @Override
    public void terminate() {
        executor.shutdown();
        try {
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.error("Timeout error", e);
        }
    }

    @Override
    public int performCullingCPU(FrustumIntersection frustum, MeshInstanceList<?> instances) {
        return performCullingCPU(frustum, instances, FrustumCullingPreCondition.NO_PRECONDITION);
    }

    @Override
    public int performCullingCPU(FrustumIntersection frustum, MeshInstanceList<?> instances, FrustumCullingPreCondition preCondition) {

        if(instances == null || instances.size() == 0) {
            return 0;
        }

        final int batchSize = (int) Math.ceil((float) instances.size() / (float) THREAD_COUNT);

        final CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT);

        for(int i = 0;i < THREAD_COUNT;i++) {

            final int batchBegin = i * batchSize;
            final int batchEnd = min(batchBegin + batchSize, instances.size());

            performCullingBatch(instances, preCondition, frustum, countDownLatch, batchBegin, batchEnd);
        }

        waitForFrustumCulling(countDownLatch);

        return baseInstance.getAndSet(0);
    }

    private void performCullingBatch(MeshInstanceList<?> instances, FrustumCullingPreCondition preCondition, FrustumIntersection frustum,
                                     CountDownLatch countDownLatch, int batchBegin, int batchEnd) {

        try(MemoryStack stack = stackPush()) {

            Vector4f sphereCenter = new Vector4f();

            GLDrawElementsCommand command = GLDrawElementsCommand.callocStack(stack);

            for(int index = batchBegin;index < batchEnd;index++) {

                MeshInstance<?> instance = instances.get(index);

                final Vector3fc scale = instance.transform().scale();
                final float maxScale = scale.get(scale.maxComponent());
                final Matrix4fc modelMatrix = instance.modelMatrix();

                setInstanceTransform(index, modelMatrix, instance.transform().normalMatrix());

                performFrustumCullingCPU(instance, command, frustum, sphereCenter, modelMatrix, index, maxScale, preCondition);
            }
        }

        countDownLatch.countDown();
    }

    private void performFrustumCullingCPU(MeshInstance<?> instance, GLDrawElementsCommand command, FrustumIntersection frustum,
                                          Vector4f sphereCenter, Matrix4fc modelMatrix, int matricesIndex,
                                          float maxScale, FrustumCullingPreCondition preCondition) {

        for(MeshView<?> meshView : instance) {

            final Mesh mesh = meshView.mesh();
            final ISphere sphere = mesh.boundingSphere();

            final FrustumCullingPreConditionState preConditionState = preCondition.getPrecondition(instance, meshView);

            if(preConditionState == FrustumCullingPreConditionState.DISCARD) {
                continue;
            }

            if(preConditionState == FrustumCullingPreConditionState.CONTINUE) {
                sphereCenter.set(sphere.center(), 1.0f).mul(modelMatrix);
            }

            final float radius = sphere.radius() * maxScale;

            if(preConditionState == FrustumCullingPreConditionState.PASS || frustum.testSphere(sphereCenter.x, sphereCenter.y, sphereCenter.z, radius)) {

                final int baseInstance = this.baseInstance.getAndIncrement();

                final int materialIndex = meshView.material().bufferIndex();

                setInstanceData(baseInstance, matricesIndex, materialIndex);

                command.count(mesh.indexCount())
                        .primCount(1)
                        .firstIndex(mesh.storageInfo().firstIndex())
                        .baseVertex(mesh.storageInfo().baseVertex())
                        .baseInstance(baseInstance);

                commandBuffer.copy(baseInstance * GLDrawElementsCommand.SIZEOF, command.buffer());
            }
        }
    }

    private void setInstanceTransform(int objectIndex, Matrix4fc modelMatrix, Matrix4fc normalMatrix) {
        try(MemoryStack stack = stackPush()) {
            ByteBuffer buffer = stack.malloc(MATRIX4_SIZEOF * 2);
            modelMatrix.get(TRANSFORMS_BUFFER_MODEL_MATRIX_OFFSET, buffer);
            normalMatrix.get(TRANSFORMS_BUFFER_NORMAL_MATRIX_OFFSET, buffer);
            transformsBuffer.copy(objectIndex * TRANSFORMS_BUFFER_MIN_SIZE, buffer);
        }
    }

    private void setInstanceData(int instanceID, int matrixIndex, int materialIndex) {

        try (MemoryStack stack = stackPush()) {

            IntBuffer buffer = stack.mallocInt(2);

            buffer.put(0, matrixIndex).put(1, materialIndex);

            instanceBuffer.copy(instanceID * INSTANCE_BUFFER_MIN_SIZE, buffer);
        }
    }

    private void waitForFrustumCulling(CountDownLatch countDownLatch) {
        try {
            countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.error("Timeout error while waiting for frustum culling", e);
        }
    }

}
