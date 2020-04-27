package naitsirc98.beryl.graphics.opengl.rendering.shadows;

import naitsirc98.beryl.graphics.opengl.rendering.GLFrustumCuller.PreConditionState;
import naitsirc98.beryl.graphics.opengl.rendering.GLIndirectRenderer;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.swapchain.GLFramebuffer;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.rendering.APIRenderSystem;
import naitsirc98.beryl.graphics.rendering.renderers.ShadowCascade;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.meshes.TerrainMesh;
import naitsirc98.beryl.meshes.views.MeshView;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.meshes.MeshInstance;
import naitsirc98.beryl.scenes.components.meshes.MeshInstanceList;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL14C.GL_DEPTH_COMPONENT32;
import static org.lwjgl.opengl.GL30C.GL_DEPTH_ATTACHMENT;

public class GLShadowCascadeRenderer {

    public static final int DEPTH_MAP_SIZE = 1024;

    private final GLTexture2D depthTexture;
    private final GLFramebuffer framebuffer;
    private final ShadowCascade shadowCascade;
    private final GLShaderProgram depthShader;

    GLShadowCascadeRenderer(GLShaderProgram depthShader) {
        this.depthShader = depthShader;
        depthTexture = createDepthTexture();
        framebuffer = createFramebuffer();
        shadowCascade = new ShadowCascade();
    }

    public ShadowCascade shadowCascade() {
        return shadowCascade;
    }

    public GLTexture2D depthTexture() {
        return depthTexture;
    }

    public void render(Scene scene, DirectionalLight light, float nearPlane, float farPlane) {

        shadowCascade.update(scene.camera(), nearPlane, farPlane, light);

        framebuffer.bind();

        glViewport(0, 0, DEPTH_MAP_SIZE, DEPTH_MAP_SIZE);
        glClearColor(0, 0, 0, 0);
        glClear(GL_DEPTH_BUFFER_BIT);

        renderMeshShadows(scene, (GLIndirectRenderer) APIRenderSystem.get().getStaticMeshRenderer());
        renderMeshShadows(scene, (GLIndirectRenderer) APIRenderSystem.get().getAnimMeshRenderer());

        glFinish();
    }

    private void renderMeshShadows(Scene scene, GLIndirectRenderer renderer) {

        final MeshInstanceList<?> instances = renderer.getInstances(scene);

        final int drawCount = renderer.frustumCuller().performCullingCPU(shadowCascade.lightFrustum(), instances, this::discardTerrain);

        renderer.addDynamicState(this::setOpenGLStateAndUniforms);

        renderer.renderScene(scene, drawCount, depthShader);
    }

    private PreConditionState discardTerrain(MeshInstance<?> instance, MeshView<?> meshView) {
        return meshView.mesh().getClass() == TerrainMesh.class ? PreConditionState.DISCARD : PreConditionState.CONTINUE;
    }

    private void setOpenGLStateAndUniforms(GLShaderProgram shader) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            shader.uniformMatrix4f("u_LightProjectionViewMatrix", false, shadowCascade.lightProjectionViewMatrix().get(buffer));
        }
    }

    private GLTexture2D createDepthTexture() {

        GLTexture2D depthTexture = new GLTexture2D();

        depthTexture.allocate(1, DEPTH_MAP_SIZE, DEPTH_MAP_SIZE, GL_DEPTH_COMPONENT32);

        return depthTexture;
    }

    private GLFramebuffer createFramebuffer() {

        GLFramebuffer framebuffer = new GLFramebuffer();

        framebuffer.attach(GL_DEPTH_ATTACHMENT, depthTexture, 0);

        framebuffer.setAsDepthOnlyFramebuffer();

        framebuffer.ensureComplete();

        return framebuffer;
    }

    public void terminate() {
        depthTexture.release();
        framebuffer.release();
    }
}
