package naitsirc98.beryl.graphics.opengl.rendering.renderers;

import naitsirc98.beryl.graphics.buffers.MappedGraphicsBuffer;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.commands.GLDrawElementsCommand;
import naitsirc98.beryl.graphics.rendering.culling.FrustumCuller;
import naitsirc98.beryl.graphics.opengl.rendering.culling.GLFrustumCuller;
import naitsirc98.beryl.graphics.opengl.rendering.shadows.GLShadowsInfo;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.graphics.rendering.Renderer;
import naitsirc98.beryl.materials.MaterialManager;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.meshes.MeshInstanceList;
import org.joml.FrustumIntersection;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

import static naitsirc98.beryl.util.types.DataType.INT32_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.MATRIX4_SIZEOF;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public abstract class GLIndirectRenderer implements Renderer {

    protected static final int VERTEX_BUFFER_BINDING = 0;
    protected static final int INSTANCE_BUFFER_BINDING = 1;

    private static final int INSTANCE_BUFFER_MIN_SIZE = INT32_SIZEOF * 2;
    private static final int TRANSFORMS_BUFFER_MIN_SIZE = MATRIX4_SIZEOF * 2;


    protected GLShaderProgram shader;

    protected GLVertexArray vertexArray;

    protected GLBuffer instanceBuffer; // model matrix + material
    protected GLBuffer transformsBuffer;
    protected GLBuffer commandBuffer;
    protected GLBuffer meshIndicesBuffer;
    protected FrustumCuller frustumCuller;
    private Queue<Consumer<GLShaderProgram>> dynamicState;
    private final GLShadowsInfo shadowsInfo;
    private int visibleObjects;

    public GLIndirectRenderer(GLShadowsInfo shadowsInfo) {
        this.shadowsInfo = shadowsInfo;
    }

    @Override
    public void init() {

        initVertexArray();

        initRenderShader();

        transformsBuffer = new GLBuffer("TRANSFORMS_STORAGE_BUFFER");

        commandBuffer = new GLBuffer("INSTANCE_COMMAND_BUFFER");

        meshIndicesBuffer = new GLBuffer("MESH_INDICES_STORAGE_BUFFER");

        frustumCuller = new GLFrustumCuller(commandBuffer, transformsBuffer, instanceBuffer);

        dynamicState = new ArrayDeque<>();
    }

    @Override
    public void terminate() {

        frustumCuller.terminate();

        shader.release();

        vertexArray.release();

        instanceBuffer.release();

        transformsBuffer.release();

        commandBuffer.release();

        meshIndicesBuffer.release();
    }

    public FrustumCuller frustumCuller() {
        return frustumCuller;
    }

    public void prepare(Scene scene) {
        updateVertexArrayVertexBuffer();
        prepareInstanceBuffer(scene, getInstances(scene));
    }

    public void addDynamicState(Consumer<GLShaderProgram> state) {
        dynamicState.add(state);
    }

    public void preComputeFrustumCulling(Scene scene) {
        visibleObjects = performFrustumCullingCPU(scene);
    }

    protected void setOpenGLState(Scene scene) {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
    }

    public void render(Scene scene, boolean shadowsEnabled) {
        final int drawCount = performFrustumCullingCPU(scene);
        render(scene, drawCount, shadowsEnabled, shader);
    }

    public void renderPreComputedVisibleObjects(Scene scene, boolean shadowsEnabled) {
        render(scene, visibleObjects, shadowsEnabled, shader);
    }

    public void render(Scene scene, int drawCount, boolean shadowsEnabled) {
        render(scene, drawCount, shadowsEnabled, shader);
    }

    public void render(Scene scene, int drawCount, boolean shadowsEnabled, GLShaderProgram shader) {

        if(drawCount <= 0) {
            dynamicState.clear();
            return;
        }

        shader.bind();

        setOpenGLState(scene);

        bindShaderUniformsAndBuffers(scene, shader, shadowsEnabled);

        if(shadowsEnabled) {
            bindShadowTextures(shader);
        }

        setDynamicState(shader);

        vertexArray.bind();

        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, NULL, drawCount, 0);

        shader.unbind();
    }

    public abstract MeshInstanceList<?> getInstances(Scene scene);

    protected void updateVertexArrayVertexBuffer() {

        GLBuffer vertexBuffer = getVertexBuffer();
        GLBuffer indexBuffer = getIndexBuffer();
        int stride = getStride();

        vertexArray.setVertexBuffer(VERTEX_BUFFER_BINDING, vertexBuffer, stride);
        vertexArray.setIndexBuffer(indexBuffer);
    }

    protected abstract GLBuffer getVertexBuffer();

    protected abstract GLBuffer getIndexBuffer();

    protected abstract int getStride();

    private void bindShadowTextures(GLShaderProgram shader) {

        GLTexture2D[] dirShadowMaps = shadowsInfo.dirShadowMaps();

        for(int i = 0;i < dirShadowMaps.length;i++) {
            shader.uniformSampler("u_DirShadowMaps["+i+"]", dirShadowMaps[i], i + 5);
        }

    }

    protected void bindShaderUniformsAndBuffers(Scene scene, GLShaderProgram shader, boolean shadowsEnabled) {

        final GLBuffer lightsUniformBuffer = scene.environment().buffer();
        final GLBuffer materialsBuffer = MaterialManager.get().buffer();
        final GLBuffer cameraUniformBuffer = scene.cameraInfo().cameraBuffer();

        shader.uniformBool("u_ShadowsEnabled", shadowsEnabled);

        cameraUniformBuffer.bind(GL_UNIFORM_BUFFER, 0);

        lightsUniformBuffer.bind(GL_UNIFORM_BUFFER, 1);

        transformsBuffer.bind(GL_SHADER_STORAGE_BUFFER, 2);

        materialsBuffer.bind(GL_SHADER_STORAGE_BUFFER, 3);

        shadowsInfo.buffer().bind(GL_UNIFORM_BUFFER, 5);

        commandBuffer.bind(GL_DRAW_INDIRECT_BUFFER);
    }

    protected void setDynamicState(GLShaderProgram shader) {
        while(!dynamicState.isEmpty()) {
            dynamicState.poll().accept(shader);
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

    private int performFrustumCullingCPU(Scene scene) {
        final FrustumIntersection frustum = scene.camera().frustum();
        return frustumCuller.performCullingCPU(frustum, getInstances(scene));
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
