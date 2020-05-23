package naitsirc98.beryl.graphics.opengl.rendering.renderers;

import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.rendering.GLShadingPipeline;
import naitsirc98.beryl.graphics.opengl.rendering.culling.GLFrustumCuller;
import naitsirc98.beryl.graphics.opengl.rendering.renderers.data.GLRenderData;
import naitsirc98.beryl.graphics.opengl.rendering.shadows.GLShadowsInfo;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.rendering.Renderer;
import naitsirc98.beryl.graphics.rendering.ShadingModel;
import naitsirc98.beryl.graphics.rendering.culling.FrustumCuller;
import naitsirc98.beryl.graphics.rendering.culling.FrustumCullingPreCondition;
import naitsirc98.beryl.materials.MaterialManager;
import naitsirc98.beryl.materials.MaterialStorageHandler;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.meshes.MeshInstanceList;
import naitsirc98.beryl.scenes.environment.skybox.Skybox;
import naitsirc98.beryl.scenes.environment.skybox.SkyboxTexture;
import org.joml.FrustumIntersection;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

import static naitsirc98.beryl.graphics.opengl.shaders.UniformUtils.uniformArrayElement;
import static naitsirc98.beryl.graphics.opengl.shaders.UniformUtils.uniformStructMember;
import static naitsirc98.beryl.graphics.rendering.culling.FrustumCullingPreConditionState.CONTINUE;
import static naitsirc98.beryl.graphics.rendering.culling.FrustumCullingPreConditionState.DISCARD;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public abstract class GLIndirectRenderer implements Renderer {

    private static final String SHADOWS_ENABLED_UNIFORM_NAME = "u_ShadowsEnabled";
    private static final String DIR_SHADOW_MAPS_UNIFORM_NAME = "u_DirShadowMaps";


    protected GLRenderData renderData;
    protected FrustumCuller frustumCuller;
    private Queue<Consumer<GLShaderProgram>> dynamicState;
    private final GLShadowsInfo shadowsInfo;
    private int visibleObjects;

    public GLIndirectRenderer(GLShadowsInfo shadowsInfo) {
        this.shadowsInfo = shadowsInfo;
    }

    @Override
    public void init() {
        renderData = createRenderData();
        frustumCuller = new GLFrustumCuller(renderData);
        dynamicState = new ArrayDeque<>();
    }

    protected abstract GLRenderData createRenderData();

    @Override
    public void terminate() {
        renderData.release();
        frustumCuller.terminate();
    }

    public FrustumCuller frustumCuller() {
        return frustumCuller;
    }

    public void prepare(Scene scene) {
        renderData.update(scene, getInstances(scene));
    }

    public void addDynamicState(Consumer<GLShaderProgram> state) {
        dynamicState.add(state);
    }

    public void preComputeFrustumCulling(Scene scene) {
        visibleObjects = performFrustumCullingCPU(scene, FrustumCullingPreCondition.NO_PRECONDITION);
    }

    public void render(Scene scene, GLShadingPipeline shadingPipeline) {
        // Only draw those meshes with the current shading model
        final int drawCount = performFrustumCullingCPU(scene, (instance, meshView) ->
                shadingPipeline.accept(meshView.material().shadingModel()) ? CONTINUE : DISCARD);

        render(scene, drawCount, shadingPipeline);
    }

    public void renderPreComputedVisibleObjects(Scene scene, GLShadingPipeline shadingPipeline) {
        render(scene, visibleObjects, shadingPipeline);
    }

    public void render(Scene scene, int drawCount, GLShadingPipeline shadingPipeline) {

        if(drawCount <= 0) {
            dynamicState.clear();
            return;
        }

        final GLShaderProgram shader = shadingPipeline.getShader();

        final boolean shadowsEnabled = shadingPipeline.areShadowsEnabled();

        shader.bind();

        setOpenGLState();

        bindShaderUniformsAndBuffers(scene, shadingPipeline);

        if(shadowsEnabled) {
            bindShadowTextures(shader);
        }

        setDynamicState(shader);

        renderData.getVertexArray().bind();

        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, NULL, drawCount, 0);

        shader.unbind();
    }

    public abstract MeshInstanceList<?> getInstances(Scene scene);

    protected void setOpenGLState() {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
    }

    protected void bindShaderUniformsAndBuffers(Scene scene, GLShadingPipeline shadingPipeline) {

        final GLBuffer lightsUniformBuffer = scene.environment().buffer();
        MaterialStorageHandler<?> materialHandler = MaterialManager.get().getStorageHandler(shadingPipeline.getShadingModel());
        final GLBuffer materialsBuffer = materialHandler.buffer();
        final GLBuffer cameraUniformBuffer = scene.cameraInfo().cameraBuffer();
        final Skybox skybox = scene.environment().skybox();
        final GLShaderProgram shader = shadingPipeline.getShader();

        shader.uniformBool(SHADOWS_ENABLED_UNIFORM_NAME, shadingPipeline.areShadowsEnabled());

        cameraUniformBuffer.bind(GL_UNIFORM_BUFFER, 0);

        lightsUniformBuffer.bind(GL_UNIFORM_BUFFER, 1);

        renderData.getTransformsBuffer().bind(GL_SHADER_STORAGE_BUFFER, 2);

        materialsBuffer.bind(GL_SHADER_STORAGE_BUFFER, 3);

        shadowsInfo.buffer().bind(GL_UNIFORM_BUFFER, 5);

        if(shadingPipeline.getShadingModel() != ShadingModel.PHONG && skybox != null) {
            bindPBRShadowTextures(shader, skybox);
        }

        renderData.getCommandBuffer().bind(GL_DRAW_INDIRECT_BUFFER);
    }

    private void bindPBRShadowTextures(GLShaderProgram shader, Skybox skybox) {

        final String skyboxName = "u_Skybox";

        final SkyboxTexture skyboxTexture = skybox.texture1();

        if(skyboxTexture.irradianceMap() != null) {
            shader.uniformSampler(uniformStructMember(skyboxName, "irradianceMap"), skyboxTexture.irradianceMap(), 10);
        }

        if(skyboxTexture.prefilterMap() != null) {
            shader.uniformSampler(uniformStructMember(skyboxName, "prefilterMap"), skyboxTexture.prefilterMap(), 11);
        }

        if(skybox.brdfTexture() != null) {
            shader.uniformSampler(uniformStructMember(skyboxName, "brdfMap"), skybox.brdfTexture(), 12);
        }
    }

    protected void setDynamicState(GLShaderProgram shader) {
        while(!dynamicState.isEmpty()) {
            dynamicState.poll().accept(shader);
        }
    }

    private int performFrustumCullingCPU(Scene scene, FrustumCullingPreCondition preCondition) {
        final FrustumIntersection frustum = scene.camera().frustum();
        return frustumCuller.performCullingCPU(frustum, getInstances(scene), preCondition);
    }

    private void bindShadowTextures(GLShaderProgram shader) {

        GLTexture2D[] dirShadowMaps = shadowsInfo.dirShadowMaps();

        for(int i = 0;i < dirShadowMaps.length;i++) {
            shader.uniformSampler(uniformArrayElement(DIR_SHADOW_MAPS_UNIFORM_NAME, i), dirShadowMaps[i], i + 5);
        }
    }
}
