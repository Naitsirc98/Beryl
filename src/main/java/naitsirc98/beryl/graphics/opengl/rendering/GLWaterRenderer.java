package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.events.EventManager;
import naitsirc98.beryl.events.window.WindowResizedEvent;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.swapchain.GLFramebuffer;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.graphics.rendering.APIRenderSystem;
import naitsirc98.beryl.graphics.rendering.renderers.WaterRenderer;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.materials.WaterMaterial;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.meshes.views.WaterMeshView;
import naitsirc98.beryl.scenes.Camera;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.SceneEnhancedWater;
import naitsirc98.beryl.scenes.components.meshes.MeshInstanceList;
import naitsirc98.beryl.scenes.components.meshes.WaterMeshInstance;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.Objects;
import java.util.function.Consumer;

import static java.lang.StrictMath.max;
import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.meshes.vertices.VertexLayout.VERTEX_LAYOUT_3D;
import static naitsirc98.beryl.util.handles.LongHandle.NULL;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER;
import static org.lwjgl.system.MemoryStack.stackPush;

public class GLWaterRenderer implements WaterRenderer {

    public static final int QUAD_INDEX_COUNT = 6;
    private GLShaderProgram waterShader;

    private GLVertexArray vertexArray;
    private GLBuffer vertexBuffer;
    private GLBuffer indexBuffer;

    private GLFramebuffer reflectionFramebuffer;
    private GLFramebuffer refractionFramebuffer;

    private GLTexture2D depthTexture;

    private StaticMesh quadMesh;

    private Vector4f clipPlane;

    private Consumer<GLShaderProgram> setClipPlaneUniform;

    @Override
    public void init() {

        clipPlane = new Vector4f();

        setClipPlaneUniform = shader -> shader.uniformVector4f("u_ClipPlane", clipPlane);

        waterShader = new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(BerylFiles.getPath("shaders/water/water.vert")))
                .attach(new GLShader(FRAGMENT_STAGE).source(BerylFiles.getPath("shaders/water/water.frag")))
                .link();

        quadMesh = StaticMesh.quad();

        vertexArray = new GLVertexArray();

        vertexBuffer = new GLBuffer();
        vertexBuffer.data(quadMesh.vertexData());

        indexBuffer = new GLBuffer();
        indexBuffer.data(quadMesh.indexData());

        vertexArray.addVertexBuffer(0, VERTEX_LAYOUT_3D.attributeList(0), vertexBuffer);
        vertexArray.setIndexBuffer(indexBuffer);

        createFramebuffers();

        EventManager.addEventCallback(WindowResizedEvent.class, this::recreateFramebuffers);
    }

    @Override
    public void terminate() {
        waterShader.release();
        vertexArray.release();
        vertexBuffer.release();
        indexBuffer.release();
        reflectionFramebuffer.release();
        refractionFramebuffer.release();
        depthTexture.release();
        quadMesh = null;
    }

    @Override
    public void render(Scene scene) {

        final MeshInstanceList<WaterMeshInstance> waterInstances = scene.meshInfo().meshViewsOfType(WaterMeshView.class);

        if(waterInstances == null) {
            return;
        }

        waterShader.bind();

        setOpenGLState(scene);

        vertexArray.bind();

        try(MemoryStack stack = stackPush()) {

            FloatBuffer modelMatrixBuffer = stack.mallocFloat(16);

            for(WaterMeshInstance instance : waterInstances) {

                renderWater(modelMatrixBuffer, instance);
            }
        }

        glDisable(GL_BLEND);
    }

    private void setOpenGLState(Scene scene) {

        final Camera camera = scene.camera();
        final GLBuffer cameraBuffer = scene.cameraInfo().cameraBuffer();
        final GLBuffer lightsBuffer = scene.environment().buffer();

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        cameraBuffer.bind(GL_UNIFORM_BUFFER, 0);

        lightsBuffer.bind(GL_UNIFORM_BUFFER, 1);

        waterShader.uniformSampler("u_DepthTexture", depthTexture, 0);
        waterShader.uniformFloat("u_NearPlane", camera.nearPlane());
        waterShader.uniformFloat("u_FarPlane", camera.farPlane());
    }

    private void renderWater(FloatBuffer modelMatrixBuffer, WaterMeshInstance instance) {

        final WaterMeshView view = instance.meshView();
        final WaterMaterial material = view.material();

        waterShader.uniformMatrix4f("u_ModelMatrix", false, instance.modelMatrix().get(modelMatrixBuffer));

        waterShader.uniformFloat("u_DistortionStrength", view.distortionStrength());
        waterShader.uniformFloat("u_TextureCoordsOffset", view.texturesOffset());
        waterShader.uniformColorRGBA("u_WaterColor", view.waterColor());
        waterShader.uniformFloat("u_WaterColorStrength", view.waterColorStrength());
        waterShader.uniformFloat("u_Tiling", view.tiling());

        bindTextures(material);

        glDrawElements(GL_TRIANGLES, QUAD_INDEX_COUNT, GL_UNSIGNED_INT, NULL);

        unbindTextures(material);
    }

    private void unbindTextures(WaterMaterial material) {

        GLTexture2D reflectionMap = (GLTexture2D) material.reflectionMap();
        GLTexture2D refractionMap = (GLTexture2D) material.refractionMap();
        GLTexture2D dudvMap = (GLTexture2D) material.dudvMap();
        GLTexture2D normalMap = (GLTexture2D) material.normalMap();

        reflectionMap.unbind(1);
        refractionMap.unbind(2);

        if(dudvMap != null) {
            dudvMap.unbind(3);
        }

        if(normalMap != null) {
            normalMap.unbind(4);
        }
    }

    private void bindTextures(WaterMaterial material) {

        GLTexture2D reflectionMap = (GLTexture2D) material.reflectionMap();
        GLTexture2D refractionMap = (GLTexture2D) material.refractionMap();
        GLTexture2D dudvMap = (GLTexture2D) material.dudvMap();
        GLTexture2D normalMap = (GLTexture2D) material.normalMap();

        waterShader.uniformSampler("u_ReflectionMap", reflectionMap, 1);
        waterShader.uniformSampler("u_RefractionMap", refractionMap, 2);

        if(dudvMap != null) {
            waterShader.uniformSampler("u_DUDVMap", dudvMap, 3);
            waterShader.uniformBool("u_DUDVMapPresent", true);
        } else {
            waterShader.uniformBool("u_DUDVMapPresent", false);
        }

        if(normalMap != null) {
            waterShader.uniformSampler("u_NormalMap", normalMap, 4);
            waterShader.uniformBool("u_NormalMapPresent", true);
        } else {
            waterShader.uniformBool("u_NormalMapPresent", false);
        }
    }

    public void bakeWaterTextures(Scene scene) {

        glEnable(GL_CLIP_DISTANCE0);

        final Camera camera = scene.camera();

        final MeshInstanceList<WaterMeshInstance> waterInstances = scene.meshInfo().meshViewsOfType(WaterMeshView.class);

        if(waterInstances == null) {
            return;
        }

        final SceneEnhancedWater enhancedWater = scene.enhancedWater();

        for(WaterMeshInstance instance : waterInstances) {

            final boolean underWater = camera.position().y() - instance.transform().position().y() < 0.0f;

            if(underWater) {
                bakeWaterTexturesUnderWater(scene, enhancedWater, instance);
            } else {
                bakeWaterTexturesNormally(scene, camera, enhancedWater, instance);
            }
        }

        glDisable(GL_CLIP_DISTANCE0);
    }

    private void bakeWaterTexturesUnderWater(Scene scene, SceneEnhancedWater enhancedWater, WaterMeshInstance instance) {

        WaterMeshView waterView = instance.meshView();

        bakeWaterTexture(scene, enhancedWater, instance.meshView(), reflectionFramebuffer, waterView.material().refractionMap(), true);
    }

    private void bakeWaterTexturesNormally(Scene scene, Camera camera, SceneEnhancedWater enhancedWater, WaterMeshInstance instance) {

        WaterMeshView waterView = instance.meshView();

        final float displacement = 2 * (camera.position().y() - instance.transform().position().y());

        bakeReflectionTexture(scene, camera, enhancedWater, waterView, displacement);

        bakeRefractionTexture(scene, camera, enhancedWater, waterView, displacement);
    }

    private void bakeReflectionTexture(Scene scene, Camera camera, SceneEnhancedWater enhancedWater, WaterMeshView waterView, float displacement) {
        prepareCameraToRenderWithReflectionPerspective(camera, waterView, displacement);
        bakeWaterTexture(scene, enhancedWater, waterView, reflectionFramebuffer, waterView.material().reflectionMap(), true);
    }

    private void bakeRefractionTexture(Scene scene, Camera camera, SceneEnhancedWater enhancedWater, WaterMeshView waterView, float displacement) {
        prepareCameraToRenderWithRefractionPerspective(camera, waterView, displacement);
        bakeWaterTexture(scene, enhancedWater, waterView, refractionFramebuffer, waterView.material().refractionMap(), false);
    }

    private void prepareCameraToRenderWithReflectionPerspective(Camera camera, WaterMeshView waterView, float displacement) {

        Vector3fc position = camera.position();

        camera.position(position.x(), position.y() - displacement, position.z());

        camera.pitch(-camera.pitch());

        camera.updateMatrices();

        clipPlane.set(waterView.clipPlane());
        clipPlane.w *= -1;
    }

    private void prepareCameraToRenderWithRefractionPerspective(Camera camera, WaterMeshView waterView, float displacement) {

        Vector3fc position = camera.position();

        camera.position(position.x(), position.y() + displacement, position.z());

        camera.pitch(-camera.pitch());

        camera.updateMatrices();

        clipPlane.set(waterView.clipPlane());
        clipPlane.y *= -1;
    }

    private void bakeWaterTexture(Scene scene, SceneEnhancedWater enhancedWater, WaterMeshView waterView,
                                  GLFramebuffer framebuffer, Texture2D texture, boolean renderSkybox) {

        prepareFramebuffer(framebuffer, (GLTexture2D) texture);

        framebuffer.bind();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if(enhancedWater.isEnhanced(waterView)) {
            renderStaticMeshes(scene);
            renderAnimMeshes(scene);
        }

        if(renderSkybox) {
            renderSkybox(scene);
        }

        glFinish();
    }

    private void renderStaticMeshes(Scene scene) {
        renderMeshes(scene, (GLIndirectRenderer) APIRenderSystem.get().getStaticMeshRenderer());
    }

    private void renderAnimMeshes(Scene scene) {
        renderMeshes(scene, (GLIndirectRenderer) APIRenderSystem.get().getAnimMeshRenderer());
    }

    private void renderMeshes(Scene scene, GLIndirectRenderer renderer) {
        renderer.addDynamicShaderState(setClipPlaneUniform);
        renderer.render(scene);
    }

    private void renderSkybox(Scene scene) {

        final GLSkyboxRenderer skyboxRenderer = (GLSkyboxRenderer) APIRenderSystem.get().getSkyboxRenderer();

        skyboxRenderer.render(scene);
    }

    private void prepareFramebuffer(GLFramebuffer framebuffer, GLTexture2D colorTexture) {

        final int width = max(Window.get().width(), 1);
        final int height = max(Window.get().height(), 1);

        if(colorTexture.width() != width || colorTexture.height() != height) {
            colorTexture.reallocate(1, width, height, PixelFormat.RGBA);
        }

        if(!Objects.equals(framebuffer.get(GL_COLOR_ATTACHMENT0), colorTexture)) {
            framebuffer.attach(GL_COLOR_ATTACHMENT0, colorTexture, 0);
            framebuffer.ensureComplete();
        }
    }

    private void recreateFramebuffers(WindowResizedEvent e) {

        if(depthTexture != null) {

            depthTexture.release();
            reflectionFramebuffer.release();
            refractionFramebuffer.release();

            createFramebuffers();
        }
    }

    private void createFramebuffers() {

        final int width = max(Window.get().width(), 1);
        final int height = max(Window.get().height(), 1);

        createDepthTexture(width, height);

        reflectionFramebuffer = createFramebuffer();
        refractionFramebuffer = createFramebuffer();
    }

    private void createDepthTexture(int width, int height) {
        depthTexture = new GLTexture2D();
        depthTexture.reallocate(1, width, height, GL_DEPTH_COMPONENT24);
    }

    private GLFramebuffer createFramebuffer() {

        GLFramebuffer framebuffer = new GLFramebuffer();

        framebuffer.attach(GL_DEPTH_ATTACHMENT, depthTexture, 0);

        return framebuffer;
    }
}
