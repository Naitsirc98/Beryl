package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.buffers.MappedGraphicsBuffer;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.commands.GLDrawElementsCommand;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.graphics.rendering.Renderer;
import naitsirc98.beryl.materials.MaterialManager;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.meshes.MeshManager;
import naitsirc98.beryl.meshes.StaticMeshManager;
import naitsirc98.beryl.meshes.views.MeshView;
import naitsirc98.beryl.scenes.Camera;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.MeshInstance;
import naitsirc98.beryl.scenes.components.meshes.MeshInstanceList;
import naitsirc98.beryl.util.geometry.ISphere;
import org.joml.*;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import static naitsirc98.beryl.graphics.ShaderStage.*;
import static naitsirc98.beryl.util.types.DataType.*;
import static org.lwjgl.opengl.ARBIndirectParameters.GL_PARAMETER_BUFFER_ARB;
import static org.lwjgl.opengl.ARBIndirectParameters.glMultiDrawElementsIndirectCountARB;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL42.GL_COMMAND_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public abstract class GLIndirectRenderer implements Renderer {

    private static final int INSTANCE_BUFFER_MIN_SIZE = INT32_SIZEOF * 2;

    private static final int TRANSFORMS_BUFFER_MIN_SIZE = MATRIX4_SIZEOF * 2;
    private static final int TRANSFORMS_BUFFER_MODEL_MATRIX_OFFSET = 0;
    private static final int TRANSFORMS_BUFFER_NORMAL_MATRIX_OFFSET = MATRIX4_SIZEOF;

    private static final int VERTEX_BUFFER_BINDING = 0;
    private static final int INSTANCE_BUFFER_BINDING = 1;

    protected GLShaderProgram cullingShader;
    protected GLShaderProgram renderShader;

    protected GLVertexArray vertexArray;
    protected GLBuffer instanceBuffer; // model matrix + material + bounding sphere indices

    protected GLBuffer transformsBuffer;
    protected GLBuffer meshIndicesBuffer;

    protected GLBuffer instanceCommandBuffer;
    protected GLBuffer atomicCounterBuffer;

    protected GLIndirectRenderer() {

    }

    @Override
    public void init() {

        instanceBuffer = new GLBuffer();

        initVertexArray();

        cullingShader = new GLShaderProgram()
                .attach(new GLShader(COMPUTE_STAGE).source(BerylFiles.getPath("shaders/compute/culling.comp")))
                .link();

        renderShader = new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(BerylFiles.getPath("shaders/phong/phong_indirect.vert")))
                .attach(new GLShader(FRAGMENT_STAGE).source(BerylFiles.getPath("shaders/phong/phong_indirect.frag")))
                .link();

        transformsBuffer = new GLBuffer("TRANSFORMS_STORAGE_BUFFER");
        meshIndicesBuffer = new GLBuffer("MESH_INDICES_STORAGE_BUFFER");

        instanceCommandBuffer = new GLBuffer("INSTANCE_COMMAND_BUFFER");

        atomicCounterBuffer = new GLBuffer("ATOMIC_COUNTER_BUFFER");
        atomicCounterBuffer.allocate(UINT32_SIZEOF);
        atomicCounterBuffer.clear();
    }

    @Override
    public void terminate() {

        cullingShader.release();
        renderShader.release();

        vertexArray.release();
        instanceBuffer.release();

        transformsBuffer.release();
        meshIndicesBuffer.release();

        instanceCommandBuffer.release();
    }

    protected void setOpenGLState(Scene scene) {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glEnable(GL_CULL_FACE);
    }

    protected void performCullingPass(Scene scene, MeshInstanceList<?> instances, boolean alwaysPass) {

        final int numObjects = instances.numMeshViews();

        StaticMeshManager staticMeshManager = MeshManager.get().staticMeshManager();

        GLBuffer meshCommandBuffer = staticMeshManager.commandBuffer();
        GLBuffer boundingSpheresBuffer = staticMeshManager.boundingSpheresBuffer();
        GLBuffer frustumBuffer = scene.cameraInfo().frustumBuffer();

        cullingShader.bind();

        meshCommandBuffer.bind(GL_SHADER_STORAGE_BUFFER, 0);
        instanceCommandBuffer.bind(GL_SHADER_STORAGE_BUFFER, 1);
        boundingSpheresBuffer.bind(GL_SHADER_STORAGE_BUFFER, 2);
        transformsBuffer.bind(GL_SHADER_STORAGE_BUFFER, 3);
        meshIndicesBuffer.bind(GL_SHADER_STORAGE_BUFFER, 4);
        frustumBuffer.bind(GL_UNIFORM_BUFFER, 5);
        atomicCounterBuffer.bind(GL_ATOMIC_COUNTER_BUFFER, 6);
        cullingShader.uniformBool("u_AlwaysPass", alwaysPass);

        glDispatchCompute(numObjects, 1, 1);

        glMemoryBarrier(GL_COMMAND_BARRIER_BIT | GL_SHADER_STORAGE_BUFFER | GL_ATOMIC_COUNTER_BARRIER_BIT);
    }

    protected int performCullingPassCPU(Scene scene, MeshInstanceList<?> instances, GLVertexArray vertexArray, boolean alwaysPass) {

        prepareInstanceBuffer(scene, instances, vertexArray);

        GLBuffer commandBuffer = this.instanceCommandBuffer;

        final FrustumIntersection frustum = scene.camera().frustum();

        AtomicInteger baseInstance = new AtomicInteger();

        instances.stream().parallel().unordered().map(e -> (MeshInstance<?>) e).forEach(instance -> {

            try(MemoryStack stack = stackPush()) {

                Vector4f center = new Vector4f();

                GLDrawElementsCommand command = GLDrawElementsCommand.callocStack(stack);

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
        });

        /*

        try(MemoryStack stack = stackPush()) {

            GLDrawElementsCommand command = GLDrawElementsCommand.callocStack(stack);

            for(MeshInstance<?> instance : instances) {

                final Vector3fc scale = instance.transform().scale();
                final float maxScale = scale.get(scale.maxComponent());
                final Matrix4fc modelMatrix = instance.modelMatrix();

                for(MeshView<?> meshView : instance) {

                    final Mesh mesh = meshView.mesh();
                    final ISphere sphere = mesh.boundingSphere();

                    center.set(sphere.center(), 1.0f).mul(modelMatrix);

                    final float radius = sphere.radius() * maxScale;

                    if(alwaysPass || frustum.testSphere(center.x, center.y, center.z, radius)) {

                        setInstanceTransform(visibleObjectsCount, instance.modelMatrix(), instance.normalMatrix());

                        final int materialIndex = meshView.material().bufferIndex();

                        setInstanceData(visibleObjectsCount, visibleObjectsCount, materialIndex);

                        command.count(mesh.indexCount())
                                .primCount(1)
                                .firstIndex(mesh.firstIndex())
                                .baseVertex(mesh.baseVertex())
                                .baseInstance(visibleObjectsCount);

                        commandBuffer.copy(visibleObjectsCount * GLDrawElementsCommand.SIZEOF, command.buffer());

                        ++visibleObjectsCount;
                    }
                }
            }
        }

         */

        return baseInstance.get();
    }

    protected void renderScene(Scene scene, MeshInstanceList<?> instances, GLVertexArray vertexArray, GLShaderProgram shader) {

        final GLBuffer lightsUniformBuffer = scene.environment().buffer();
        final GLBuffer materialsBuffer = MaterialManager.get().buffer();
        final GLBuffer cameraUniformBuffer = scene.cameraInfo().cameraBuffer();

        setOpenGLState(scene);

        shader.bind();

        cameraUniformBuffer.bind(GL_UNIFORM_BUFFER, 0);

        lightsUniformBuffer.bind(GL_UNIFORM_BUFFER, 1);

        transformsBuffer.bind(GL_SHADER_STORAGE_BUFFER, 2);

        materialsBuffer.bind(GL_SHADER_STORAGE_BUFFER, 3);

        instanceCommandBuffer.bind(GL_DRAW_INDIRECT_BUFFER);

        atomicCounterBuffer.bind(GL_PARAMETER_BUFFER_ARB);

        vertexArray.bind();

        glMultiDrawElementsIndirectCountARB(GL_TRIANGLES, GL_UNSIGNED_INT, NULL, 0, instances.numMeshViews(), 0);
    }

    protected void renderScene(Scene scene, int drawCount, GLVertexArray vertexArray, GLShaderProgram shader) {

        final GLBuffer lightsUniformBuffer = scene.environment().buffer();
        final GLBuffer materialsBuffer = MaterialManager.get().buffer();
        final GLBuffer cameraUniformBuffer = scene.cameraInfo().cameraBuffer();

        setOpenGLState(scene);

        shader.bind();

        cameraUniformBuffer.bind(GL_UNIFORM_BUFFER, 0);

        lightsUniformBuffer.bind(GL_UNIFORM_BUFFER, 1);

        transformsBuffer.bind(GL_SHADER_STORAGE_BUFFER, 2);

        materialsBuffer.bind(GL_SHADER_STORAGE_BUFFER, 3);

        instanceCommandBuffer.bind(GL_DRAW_INDIRECT_BUFFER);

        vertexArray.bind();

        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, NULL, drawCount, 0);
    }

    protected void prepareInstanceBuffer(Scene scene, MeshInstanceList<?> instances, GLVertexArray vertexArray) {

        final int numObjects = instances.numMeshViews();

        final int instanceCommandsMinSize = numObjects * GLDrawElementsCommand.SIZEOF;

        if (instanceCommandBuffer.size() < instanceCommandsMinSize) {
            reallocateBuffer(instanceCommandBuffer, instanceCommandsMinSize);
        }

        // clearCommandBuffer();

        final int instancesMinSize = numObjects * INSTANCE_BUFFER_MIN_SIZE;

        if (instanceBuffer.size() < instancesMinSize) {
            reallocateBuffer(instanceBuffer, instancesMinSize);
            vertexArray.setVertexBuffer(INSTANCE_BUFFER_BINDING, instanceBuffer, INSTANCE_BUFFER_MIN_SIZE);
        }

        /*

        final long meshIDsMinSize = numObjects * UINT32_SIZEOF;

        if (meshIndicesBuffer.size() < meshIDsMinSize) {
            reallocateBuffer(meshIndicesBuffer, meshIDsMinSize);
        }

         */

        final int transformsMinSize = numObjects * TRANSFORMS_BUFFER_MIN_SIZE;

        if (transformsBuffer.size() < transformsMinSize) {
            reallocateBuffer(transformsBuffer, transformsMinSize);
        }

        /*

        int objectIndex = 0;

        for(MeshInstance<?> instance : instances) {

            for(MeshView<?> meshView : instance) {

                final int meshIndex = meshView.mesh().index();

                setInstanceTransform(objectIndex, instance.modelMatrix(), instance.normalMatrix());

                setInstanceMeshIndex(objectIndex, meshIndex);

                final int materialIndex = meshView.material().bufferIndex();

                setInstanceData(objectIndex, objectIndex, materialIndex);

                ++objectIndex;
            }

        }

         */
    }

    public void clearCommandBuffer() {
        instanceCommandBuffer.clear();
    }

    protected void setInstanceMeshIndex(int objectIndex, int meshIndex) {
        try (MemoryStack stack = stackPush()) {
            meshIndicesBuffer.copy(objectIndex * UINT32_SIZEOF, stack.ints(meshIndex));
        }
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

    protected void reallocateBuffer(MappedGraphicsBuffer buffer, long size) {
        buffer.unmapMemory();
        buffer.reallocate(size);
        buffer.mapMemory();
    }

    protected abstract void initVertexArray();
}
