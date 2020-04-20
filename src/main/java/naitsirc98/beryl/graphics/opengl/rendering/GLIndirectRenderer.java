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
import naitsirc98.beryl.meshes.MeshManager;
import naitsirc98.beryl.meshes.StaticMeshManager;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.meshes.MeshInstanceList;
import org.lwjgl.system.MemoryStack;

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

    protected GLFrustumCuller frustumCuller;

    protected GLIndirectRenderer() {

    }

    @Override
    public void init() {

        instanceBuffer = new GLBuffer();

        initVertexArray();

        initRenderShader();

        cullingShader = new GLShaderProgram()
                .attach(new GLShader(COMPUTE_STAGE).source(BerylFiles.getPath("shaders/compute/culling.comp")))
                .link();

        transformsBuffer = new GLBuffer("TRANSFORMS_STORAGE_BUFFER");
        meshIndicesBuffer = new GLBuffer("MESH_INDICES_STORAGE_BUFFER");

        instanceCommandBuffer = new GLBuffer("INSTANCE_COMMAND_BUFFER");

        atomicCounterBuffer = new GLBuffer("ATOMIC_COUNTER_BUFFER");
        atomicCounterBuffer.allocate(UINT32_SIZEOF);
        atomicCounterBuffer.clear();

        frustumCuller = new GLFrustumCuller(instanceCommandBuffer, transformsBuffer, instanceBuffer);
    }

    @Override
    public void terminate() {

        frustumCuller.terminate();

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

        return frustumCuller.performCullingCPU(scene, instances, alwaysPass);
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

        if(instances == null) {
            return;
        }

        final int numObjects = instances.numMeshViews();

        if(numObjects == 0) {
            return;
        }

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


    protected void reallocateBuffer(MappedGraphicsBuffer buffer, long size) {
        buffer.unmapMemory();
        buffer.reallocate(size);
        buffer.mapMemory();
    }

    protected abstract void initVertexArray();

    protected abstract void initRenderShader();
}
