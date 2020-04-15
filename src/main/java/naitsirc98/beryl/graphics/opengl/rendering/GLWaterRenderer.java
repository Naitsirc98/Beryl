package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.events.EventManager;
import naitsirc98.beryl.events.window.WindowResizedEvent;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.swapchain.GLFramebuffer;
import naitsirc98.beryl.graphics.opengl.swapchain.GLRenderbuffer;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.graphics.rendering.renderers.WaterRenderer;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.materials.WaterMaterial;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.meshes.models.StaticMeshLoader;
import naitsirc98.beryl.meshes.views.WaterMeshView;
import naitsirc98.beryl.scenes.Camera;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.SceneEnhancedWater;
import naitsirc98.beryl.scenes.components.meshes.MeshInstanceList;
import naitsirc98.beryl.scenes.components.meshes.WaterMeshInstance;
import naitsirc98.beryl.util.geometry.Sizec;
import org.joml.*;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.meshes.vertices.VertexLayout.VERTEX_LAYOUT_3D;
import static naitsirc98.beryl.util.handles.LongHandle.NULL;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER;
import static org.lwjgl.system.MemoryStack.stackPush;

public class GLWaterRenderer implements WaterRenderer {

    private GLShaderProgram waterShader;

    private GLVertexArray vertexArray;
    private GLBuffer vertexBuffer;
    private GLBuffer indexBuffer;

    private GLFramebuffer framebuffer;
    private GLRenderbuffer depthBuffer;

    private StaticMesh quadMesh;

    @Override
    public void init() {

        waterShader = new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(BerylFiles.getPath("shaders/water/water.vert")))
                .attach(new GLShader(FRAGMENT_STAGE).source(BerylFiles.getPath("shaders/water/water.frag")))
                .link();

        quadMesh = StaticMeshLoader.get().load(BerylFiles.getPath("models/quad.obj")).loadedMesh(0).mesh();

        vertexArray = new GLVertexArray();

        vertexBuffer = new GLBuffer();
        vertexBuffer.data(quadMesh.vertexData());

        indexBuffer = new GLBuffer();
        indexBuffer.data(quadMesh.indexData());

        vertexArray.addVertexBuffer(0, VERTEX_LAYOUT_3D.attributeList(0), vertexBuffer);
        vertexArray.setIndexBuffer(indexBuffer);

        createFramebuffer();

        EventManager.addEventCallback(WindowResizedEvent.class, this::recreateFramebuffer);
    }

    @Override
    public void terminate() {
        waterShader.release();
        vertexArray.release();
        vertexBuffer.release();
        indexBuffer.release();
        framebuffer.release();
        depthBuffer.release();
        quadMesh = null;
    }

    public void bakeWaterTextures(Scene scene, GLStaticMeshRenderer staticMeshRenderer, GLSkyboxRenderer skyboxRenderer) {

        glEnable(GL_CLIP_DISTANCE0);

        final Sizec size = Window.get().size();

        final Camera camera = scene.camera();

        final MeshInstanceList<WaterMeshInstance> waterInstances = scene.meshInfo().meshViewsOfType(WaterMeshView.class);

        final SceneEnhancedWater enhancedWater = scene.enhancedWater();

        final Vector3fc cameraPosition = camera.position();

        Vector4f clipPlane = new Vector4f();

        final float pitch = camera.pitch();

        for(WaterMeshInstance instance : waterInstances) {

            WaterMeshView waterView = instance.meshView();

            final float displacement = 2 * (cameraPosition.y() - instance.transform().position().y());

            camera.position(cameraPosition.x(), cameraPosition.y() - displacement, cameraPosition.z());
            camera.pitch(-pitch);
            camera.update();

            clipPlane.set(waterView.clipPlane());

            clipPlane.w *= -1;

            bakeWaterTexture(scene, enhancedWater, staticMeshRenderer, skyboxRenderer, waterView, clipPlane, size,
                    (GLTexture2D) waterView.material().reflectionMap(), true);

            camera.position(cameraPosition.x(), cameraPosition.y() + displacement, cameraPosition.z());
            camera.pitch(pitch);
            camera.update();

            clipPlane.set(waterView.clipPlane());

            clipPlane.y *= -1;

            bakeWaterTexture(scene, enhancedWater, staticMeshRenderer, skyboxRenderer, waterView, clipPlane, size,
                    (GLTexture2D) waterView.material().refractionMap(), false);
        }

        staticMeshRenderer.renderShader.uniformVector4f("u_ClipPlane", 0, 0, 0, 0);

        glDisable(GL_CLIP_DISTANCE0);
    }

    private void bakeWaterTexture(Scene scene, SceneEnhancedWater enhancedWater,
                                  GLStaticMeshRenderer staticMeshRenderer, GLSkyboxRenderer skyboxRenderer,
                                  WaterMeshView waterView, Vector4fc clipPlane, Sizec size, GLTexture2D texture, boolean renderSkybox) {

        prepareFramebuffer(size, texture);

        framebuffer.bind();

        if(enhancedWater.isEnhanced(waterView)) {
            staticMeshRenderer.performCullingPass(scene, staticMeshRenderer.getStaticInstances(scene), false);
            staticMeshRenderer.renderShader.bind();
            staticMeshRenderer.renderShader.uniformVector4f("u_ClipPlane", clipPlane);
            staticMeshRenderer.render(scene);
        }

        if(renderSkybox) {
            skyboxRenderer.render(scene);
        }

        glFinish();
    }

    private void prepareFramebuffer(Sizec size, GLTexture2D colorTexture) {

        if(colorTexture.width() != size.width() || colorTexture.height() != size.height()) {
            colorTexture.reallocate(1, size.width(), size.height(), PixelFormat.RGBA);
        }

        framebuffer.attach(GL_COLOR_ATTACHMENT0, colorTexture, 0);

        framebuffer.ensureComplete();
    }

    @Override
    public void render(Scene scene) {

        final MeshInstanceList<WaterMeshInstance> waterInstances = scene.meshInfo().meshViewsOfType(WaterMeshView.class);
        final GLShaderProgram waterShader = this.waterShader;
        final int indexCount = quadMesh.indexCount();
        final GLBuffer cameraBuffer = scene.cameraInfo().cameraBuffer();
        final GLBuffer lightsBuffer = scene.environment().buffer();

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);

        waterShader.bind();

        cameraBuffer.bind(GL_UNIFORM_BUFFER, 0);

        lightsBuffer.bind(GL_UNIFORM_BUFFER, 1);

        vertexArray.bind();

        try(MemoryStack stack = stackPush()) {

            FloatBuffer modelMatrixBuffer = stack.mallocFloat(16);

            for(WaterMeshInstance instance : waterInstances) {

                final WaterMeshView view = instance.meshView();
                final WaterMaterial material = view.material();

                waterShader.uniformMatrix4f("u_ModelMatrix", false, instance.modelMatrix().get(modelMatrixBuffer));

                waterShader.uniformFloat("u_DistortionStrength", view.distortionStrength());
                waterShader.uniformFloat("u_TextureCoordsOffset", view.texturesOffset());
                waterShader.uniformColorRGBA("u_WaterColor", view.waterColor());
                waterShader.uniformFloat("u_WaterColorStrength", view.waterColorStrength());
                waterShader.uniformFloat("u_Tiling", view.tiling());

                bindTextures(material);

                glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, NULL);

                unbindTextures(material);
            }
        }
    }

    private void unbindTextures(WaterMaterial material) {

        GLTexture2D reflectionMap = (GLTexture2D) material.reflectionMap();
        GLTexture2D refractionMap = (GLTexture2D) material.refractionMap();
        GLTexture2D dudvMap = (GLTexture2D) material.dudvMap();
        GLTexture2D normalMap = (GLTexture2D) material.normalMap();

        reflectionMap.unbind(0);
        refractionMap.unbind(1);

        if(dudvMap != null) {
            dudvMap.unbind(2);
        }

        if(normalMap != null) {
            normalMap.unbind(3);
        }
    }

    private void bindTextures(WaterMaterial material) {

        GLTexture2D reflectionMap = (GLTexture2D) material.reflectionMap();
        GLTexture2D refractionMap = (GLTexture2D) material.refractionMap();
        GLTexture2D dudvMap = (GLTexture2D) material.dudvMap();
        GLTexture2D normalMap = (GLTexture2D) material.normalMap();

        waterShader.uniformSampler("u_ReflectionMap", reflectionMap, 0);
        waterShader.uniformSampler("u_RefractionMap", refractionMap, 1);

        if(dudvMap != null) {
            waterShader.uniformSampler("u_DUDVMap", dudvMap, 2);
            waterShader.uniformBool("u_DUDVMapPresent", true);
        } else {
            waterShader.uniformBool("u_DUDVMapPresent", false);
        }

        if(normalMap != null) {
            waterShader.uniformSampler("u_NormalMap", normalMap, 3);
            waterShader.uniformBool("u_NormalMapPresent", true);
        } else {
            waterShader.uniformBool("u_NormalMapPresent", false);
        }
    }

    private void createFramebuffer() {

        framebuffer = new GLFramebuffer();

        Sizec size = Window.get().size();

        depthBuffer = new GLRenderbuffer();
        depthBuffer.storage(size.width(), size.height(), GL_DEPTH_COMPONENT16);

        framebuffer.attach(GL_DEPTH_ATTACHMENT, depthBuffer);
    }

    private void recreateFramebuffer(WindowResizedEvent e) {
        if(framebuffer != null) {
            framebuffer.release();
            createFramebuffer();
        }
    }
}
