package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.graphics.buffers.MappedGraphicsBuffer;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.commands.GLDrawElementsCommand;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.graphics.rendering.Renderer;
import naitsirc98.beryl.materials.MaterialManager;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.meshes.MeshInstanceList;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

import static naitsirc98.beryl.util.types.DataType.INT32_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.MATRIX4_SIZEOF;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public abstract class GLIndirectRenderer implements Renderer {

    private static final int INSTANCE_BUFFER_MIN_SIZE = INT32_SIZEOF * 2;

    private static final int TRANSFORMS_BUFFER_MIN_SIZE = MATRIX4_SIZEOF * 2;
    private static final int TRANSFORMS_BUFFER_MODEL_MATRIX_OFFSET = 0;
    private static final int TRANSFORMS_BUFFER_NORMAL_MATRIX_OFFSET = MATRIX4_SIZEOF;

    private static final int VERTEX_BUFFER_BINDING = 0;
    private static final int INSTANCE_BUFFER_BINDING = 1;

    protected GLShaderProgram renderShader;

    protected GLVertexArray vertexArray;
    protected GLBuffer instanceBuffer; // model matrix + material + bounding sphere indices

    protected GLBuffer transformsBuffer;

    protected GLBuffer commandBuffer;

    protected GLFrustumCuller frustumCuller;

    private Queue<Consumer<GLShaderProgram>> dynamicShaderState;

    protected GLIndirectRenderer() {

    }

    @Override
    public void init() {

        initVertexArray();

        initRenderShader();

        transformsBuffer = new GLBuffer("TRANSFORMS_STORAGE_BUFFER");

        commandBuffer = new GLBuffer("INSTANCE_COMMAND_BUFFER");

        frustumCuller = new GLFrustumCuller(commandBuffer, transformsBuffer, instanceBuffer);

        dynamicShaderState = new ArrayDeque<>();
    }

    @Override
    public void terminate() {

        frustumCuller.terminate();

        renderShader.release();

        vertexArray.release();
        instanceBuffer.release();

        transformsBuffer.release();

        commandBuffer.release();
    }

    public void prepare(Scene scene) {
        updateVertexArrayVertexBuffer();
        prepareInstanceBuffer(scene, getInstances(scene));
    }

    @Override
    public void render(Scene scene) {
        final int drawCount = frustumCuller.performCullingCPU(scene, getInstances(scene), false);
        renderScene(scene, drawCount);
    }

    public void addDynamicShaderState(Consumer<GLShaderProgram> state) {
        dynamicShaderState.add(state);
    }

    protected abstract void updateVertexArrayVertexBuffer();

    protected void setOpenGLState(Scene scene) {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glEnable(GL_CULL_FACE);
    }

    protected abstract MeshInstanceList<?> getInstances(Scene scene);

    /*

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

    */

    protected void renderScene(Scene scene, int drawCount) {

        if(drawCount <= 0) {
            dynamicShaderState.clear();
            return;
        }

        renderShader.bind();

        setOpenGLState(scene);

        bindShaderBuffers(scene);

        setDynamicShaderState();

        vertexArray.bind();

        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, NULL, drawCount, 0);
    }

    protected void bindShaderBuffers(Scene scene) {

        final GLBuffer lightsUniformBuffer = scene.environment().buffer();
        final GLBuffer materialsBuffer = MaterialManager.get().buffer();
        final GLBuffer cameraUniformBuffer = scene.cameraInfo().cameraBuffer();

        cameraUniformBuffer.bind(GL_UNIFORM_BUFFER, 0);

        lightsUniformBuffer.bind(GL_UNIFORM_BUFFER, 1);

        transformsBuffer.bind(GL_SHADER_STORAGE_BUFFER, 2);

        materialsBuffer.bind(GL_SHADER_STORAGE_BUFFER, 3);

        commandBuffer.bind(GL_DRAW_INDIRECT_BUFFER);
    }

    protected void setDynamicShaderState() {
        while(!dynamicShaderState.isEmpty()) {
            dynamicShaderState.poll().accept(renderShader);
        }
    }

    protected abstract void initVertexArray();

    protected abstract void initRenderShader();

    protected boolean prepareInstanceBuffer(Scene scene, MeshInstanceList<?> instances) {

        final int numObjects = instances == null ? 0 : instances.numMeshViews();

        if(numObjects == 0) {
            return false;
        }

        checkCommandBuffer(numObjects);

        checkPerInstanceDataBuffer(numObjects);

        checkTransformsBuffer(numObjects);

        return true;
    }

    private void checkTransformsBuffer(int numObjects) {

        final int transformsMinSize = numObjects * TRANSFORMS_BUFFER_MIN_SIZE;

        if (transformsBuffer.size() < transformsMinSize) {
            reallocateBuffer(transformsBuffer, transformsMinSize);
        }
    }

    private void checkPerInstanceDataBuffer(int numObjects) {

        final int instancesMinSize = numObjects * INSTANCE_BUFFER_MIN_SIZE;

        if (instanceBuffer.size() < instancesMinSize) {
            reallocateBuffer(instanceBuffer, instancesMinSize);
            vertexArray.setVertexBuffer(INSTANCE_BUFFER_BINDING, instanceBuffer, INSTANCE_BUFFER_MIN_SIZE);
        }
    }

    private void checkCommandBuffer(int numObjects) {

        final int commandBufferMinSize = numObjects * GLDrawElementsCommand.SIZEOF;

        if(commandBuffer.size() < commandBufferMinSize) {
            reallocateBuffer(commandBuffer, commandBufferMinSize);
        }
    }

    private void reallocateBuffer(MappedGraphicsBuffer buffer, long size) {

        buffer.unmapMemory();

        buffer.reallocate(size);

        if(!buffer.mapped()) {
            buffer.mapMemory();
        }
    }
}
