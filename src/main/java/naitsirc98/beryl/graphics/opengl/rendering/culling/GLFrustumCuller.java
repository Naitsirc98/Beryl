package naitsirc98.beryl.graphics.opengl.rendering.culling;

import naitsirc98.beryl.graphics.opengl.commands.GLCommandBuilder;
import naitsirc98.beryl.graphics.opengl.commands.GLDrawElementsCommand;
import naitsirc98.beryl.graphics.opengl.rendering.renderers.data.GLRenderData;
import naitsirc98.beryl.graphics.rendering.culling.FrustumCuller;
import naitsirc98.beryl.graphics.rendering.culling.FrustumCullingPreCondition;
import naitsirc98.beryl.graphics.rendering.culling.FrustumCullingPreConditionState;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.meshes.views.MeshView;
import naitsirc98.beryl.scenes.components.meshes.MeshInstance;
import naitsirc98.beryl.scenes.components.meshes.MeshInstanceList;
import naitsirc98.beryl.util.geometry.ISphere;
import org.joml.FrustumIntersection;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.min;
import static naitsirc98.beryl.core.BerylConfigConstants.GRAPHICS_MULTITHREADING_ENABLED;
import static naitsirc98.beryl.graphics.rendering.culling.FrustumCullingPreConditionState.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class GLFrustumCuller implements FrustumCuller {

    private static final int MAX_NUM_BATCHES = 64;


    private final ExecutorService executor;
    private final GLCommandBuilder commandBuilder;
    // Per run attributes
    private FrustumIntersection frustum;
    private MeshInstanceList<?> instances;
    private FrustumCullingPreCondition preCondition;

    public GLFrustumCuller(GLRenderData renderData) {
        this.executor = Executors.newCachedThreadPool();
        commandBuilder = new GLCommandBuilder(renderData);
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

        setRunAttributes(frustum, instances, preCondition);

        runFrustumCullingCPU();

        clearRunAttributes();

        return commandBuilder.count();
    }

    private void runFrustumCullingCPU() {
        if(GRAPHICS_MULTITHREADING_ENABLED) {
            runFrustumCullingCPUInParallel();
        } else {
            runFrustumCullingCPUSingleThread();
        }
    }

    private void runFrustumCullingCPUInParallel() {

        final int batchSize = (int) Math.ceil((float) instances.size() / (float) MAX_NUM_BATCHES);

        final CountDownLatch countDownLatch = new CountDownLatch(MAX_NUM_BATCHES);

        for(int i = 0; i < MAX_NUM_BATCHES; i++) {

            final int batchBegin = i * batchSize;
            final int batchEnd = min(batchBegin + batchSize, instances.size());

            executor.execute(() -> performCullingBatch(countDownLatch, batchBegin, batchEnd));
        }

        waitForFrustumCulling(countDownLatch);
    }

    private void runFrustumCullingCPUSingleThread() {
        performFrustumCullingCPURange(0, instances.size());
    }

    private void performCullingBatch(CountDownLatch countDownLatch, int batchBegin, int batchEnd) {
        performFrustumCullingCPURange(batchBegin, batchEnd);
        countDownLatch.countDown();
    }

    private void performFrustumCullingCPURange(int beginIndex, int endIndex) {

        try(MemoryStack stack = stackPush()) {

            Vector4f sphereCenter = new Vector4f();

            GLDrawElementsCommand command = GLDrawElementsCommand.callocStack(stack);

            for(int index = beginIndex;index < endIndex;index++) {

                MeshInstance<?> instance = instances.get(index);

                final Vector3fc scale = instance.transform().scale();
                final float maxScale = scale.get(scale.maxComponent());
                final Matrix4fc modelMatrix = instance.modelMatrix();

                commandBuilder.setInstanceTransform(index, modelMatrix, instance.transform().normalMatrix());

                performFrustumCullingCPU(instance, command, sphereCenter, modelMatrix, index, maxScale);
            }
        }
    }

    private void performFrustumCullingCPU(MeshInstance<?> instance, GLDrawElementsCommand command,
                                          Vector4f center, Matrix4fc modelMatrix, int matricesIndex,
                                          float maxScale) {

        for(MeshView<?> meshView : instance) {

            final Mesh mesh = meshView.mesh();
            final ISphere sphere = mesh.boundingSphere();

            final FrustumCullingPreConditionState preConditionState = preCondition.compute(instance, meshView);

            if(preConditionState == DISCARD) {
                continue;
            }

            if(preConditionState == CONTINUE) {
                center.set(sphere.center(), 1.0f).mul(modelMatrix);
            }

            final float radius = sphere.radius() * maxScale;

            if(preConditionState == PASS || frustum.testSphere(center.x, center.y, center.z, radius)) {

                commandBuilder.buildDrawCommand(command, matricesIndex, meshView, mesh);
            }
        }
    }

    private void waitForFrustumCulling(CountDownLatch countDownLatch) {
        try {
            countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.error("Timeout error while waiting for frustum culling", e);
        }
    }

    private void setRunAttributes(FrustumIntersection frustum, MeshInstanceList<?> instances, FrustumCullingPreCondition preCondition) {
        this.frustum = frustum;
        this.instances = instances;
        this.preCondition = preCondition;
    }

    private void clearRunAttributes() {
        frustum = null;
        instances = null;
        preCondition = null;
    }
}
