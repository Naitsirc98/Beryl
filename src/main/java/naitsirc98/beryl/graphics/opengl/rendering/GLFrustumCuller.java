package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.commands.GLDrawElementsCommand;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
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
import static naitsirc98.beryl.graphics.ShaderStage.COMPUTE_STAGE;
import static naitsirc98.beryl.util.types.DataType.*;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL42.*;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43.glDispatchCompute;
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
    protected GLBuffer meshIndicesBuffer;
    private final GLBuffer instanceBuffer;
    private final GLShaderProgram cullingShader;
    private final GLBuffer atomicCounterBuffer;

    public GLFrustumCuller(GLBuffer commandBuffer, GLBuffer transformsBuffer, GLBuffer instanceBuffer) {
        this.commandBuffer = commandBuffer;
        this.transformsBuffer = transformsBuffer;
        this.instanceBuffer = instanceBuffer;
        this.executor = Executors.newFixedThreadPool(THREAD_COUNT);
        baseInstance = new AtomicInteger();
        meshIndicesBuffer = new GLBuffer("MESH_INDICES_STORAGE_BUFFER");
        cullingShader = createCullingShader();
        atomicCounterBuffer = createAtomicCounterBuffer();
    }

    public int performCullingCPU(Scene scene, MeshInstanceList<?> instances, boolean alwaysPass) {

        if(instances == null || instances.size() == 0) {
            return 0;
        }

        final int batchSize = (int) Math.ceil((float) instances.size() / (float) THREAD_COUNT);

        final FrustumIntersection frustum = scene.camera().frustum();

        final CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT);

        for(int i = 0;i < THREAD_COUNT;i++) {

            final int batchBegin = i * batchSize;
            final int batchEnd = min(batchBegin + batchSize, instances.size());

            performCullingBatch(instances, alwaysPass, frustum, countDownLatch, batchBegin, batchEnd);
        }

        waitForFrustumCulling(countDownLatch);

        return baseInstance.getAndSet(0);
    }

    public void performCullingPassGPU(Scene scene, int totalObjects, GLBuffer meshCommandBuffer, GLBuffer boundingSpheresBuffer, boolean alwaysPass) {

        GLBuffer frustumBuffer = scene.cameraInfo().frustumBuffer();

        cullingShader.bind();

        meshCommandBuffer.bind(GL_SHADER_STORAGE_BUFFER, 0);
        commandBuffer.bind(GL_SHADER_STORAGE_BUFFER, 1);
        boundingSpheresBuffer.bind(GL_SHADER_STORAGE_BUFFER, 2);
        transformsBuffer.bind(GL_SHADER_STORAGE_BUFFER, 3);
        meshIndicesBuffer.bind(GL_SHADER_STORAGE_BUFFER, 4);
        frustumBuffer.bind(GL_UNIFORM_BUFFER, 5);
        atomicCounterBuffer.bind(GL_ATOMIC_COUNTER_BUFFER, 6);
        cullingShader.uniformBool("u_AlwaysPass", alwaysPass);

        glDispatchCompute(totalObjects, 1, 1);

        glMemoryBarrier(GL_ALL_BARRIER_BITS);
    }

    public GLBuffer atomicCounter() {
        return atomicCounterBuffer;
    }

    private void performCullingBatch(MeshInstanceList<?> instances, boolean alwaysPass, FrustumIntersection frustum,
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

                performFrustumCullingCPU(instance, command, frustum, sphereCenter, modelMatrix, index, maxScale, alwaysPass);
            }
        }

        countDownLatch.countDown();
    }

    private void performFrustumCullingCPU(MeshInstance<?> instance, GLDrawElementsCommand command, FrustumIntersection frustum,
                                          Vector4f sphereCenter, Matrix4fc modelMatrix, int matricesIndex,
                                          float maxScale, boolean alwaysPass) {

        for(MeshView<?> meshView : instance) {

            final Mesh mesh = meshView.mesh();
            final ISphere sphere = mesh.boundingSphere();

            if(!alwaysPass) {
                sphereCenter.set(sphere.center(), 1.0f).mul(modelMatrix);
            }

            final float radius = sphere.radius() * maxScale;

            if(alwaysPass || frustum.testSphere(sphereCenter.x, sphereCenter.y, sphereCenter.z, radius)) {

                final int baseInstance = this.baseInstance.getAndIncrement();

                final int materialIndex = meshView.material().bufferIndex();

                setInstanceData(baseInstance, matricesIndex, materialIndex);

                command.count(mesh.indexCount())
                        .primCount(1)
                        .firstIndex(mesh.firstIndex())
                        .baseVertex(mesh.baseVertex())
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

    private void setInstanceMeshIndex(int objectIndex, int meshIndex) {
        try (MemoryStack stack = stackPush()) {
            meshIndicesBuffer.copy(objectIndex * UINT32_SIZEOF, stack.ints(meshIndex));
        }
    }

    private void waitForFrustumCulling(CountDownLatch countDownLatch) {
        try {
            countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.error("Timeout error while waiting for frustum culling", e);
        }
    }

    private GLShaderProgram createCullingShader() {
        return new GLShaderProgram()
                .attach(new GLShader(COMPUTE_STAGE).source(BerylFiles.getPath("shaders/compute/culling.comp")))
                .link();
    }

    private GLBuffer createAtomicCounterBuffer() {
        GLBuffer atomicCounterBuffer = new GLBuffer("ATOMIC_COUNTER_BUFFER");
        atomicCounterBuffer.allocate(UINT32_SIZEOF);
        atomicCounterBuffer.clear();
        return atomicCounterBuffer;
    }

    void terminate() {

        meshIndicesBuffer.release();

        atomicCounterBuffer.release();

        cullingShader.release();

        shutdown();
    }

    private void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.error("Timeout error", e);
        }
    }

}
