package naitsirc98.beryl.graphics.opengl.rendering.shadows;

import naitsirc98.beryl.graphics.opengl.rendering.GLFrustumCuller.PreConditionState;
import naitsirc98.beryl.graphics.opengl.rendering.GLIndirectRenderer;
import naitsirc98.beryl.graphics.opengl.rendering.GLMeshRenderer;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.swapchain.GLFramebuffer;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.rendering.ShadowCascade;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.meshes.TerrainMesh;
import naitsirc98.beryl.meshes.views.MeshView;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.meshes.MeshInstance;
import naitsirc98.beryl.scenes.components.meshes.MeshInstanceList;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static java.lang.Math.max;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL14C.GL_DEPTH_COMPONENT32;
import static org.lwjgl.opengl.GL30C.GL_DEPTH_ATTACHMENT;

public class GLShadowCascadeRenderer {

    private final GLTexture2D depthTexture;
    private final GLFramebuffer framebuffer;
    private final ShadowCascade shadowCascade;
    private final GLShaderProgram depthShader;

    GLShadowCascadeRenderer(GLShaderProgram depthShader) {
        this.depthShader = depthShader;
        depthTexture = new GLTexture2D();
        framebuffer = new GLFramebuffer();
        framebuffer.setAsDepthOnlyFramebuffer();
        shadowCascade = new ShadowCascade();
    }

    public ShadowCascade shadowCascade() {
        return shadowCascade;
    }

    public GLTexture2D depthTexture() {
        return depthTexture;
    }

    public void render(Scene scene, GLMeshRenderer meshRenderer, DirectionalLight light, float nearPlane, float farPlane) {

        shadowCascade.update(scene.camera(), nearPlane, farPlane, light);

        prepareFramebuffer(scene);

        renderMeshShadows(scene, meshRenderer.staticMeshRenderer());

        glFinish();
    }

    private void renderMeshShadows(Scene scene, GLIndirectRenderer renderer) {

        final MeshInstanceList<?> instances = renderer.getInstances(scene);

        final int drawCount = renderer.frustumCuller().performCullingCPU(shadowCascade.lightFrustum(), instances, this::discardTerrain);

        renderer.addDynamicState(this::setOpenGLStateAndUniforms);

        renderer.render(scene, drawCount, false, depthShader);
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

    private void prepareFramebuffer(Scene scene) {

        final int shadowMapSize = max(scene.environment().lighting().shadowMapSize(), 1);

        if(depthTexture.width() != shadowMapSize) {
            setupFramebuffer(shadowMapSize);
        }

        framebuffer.bind();

        glViewport(0, 0, shadowMapSize, shadowMapSize);
        glClearColor(0, 0, 0, 0);
        glClear(GL_DEPTH_BUFFER_BIT);
    }

    private void setupFramebuffer(int shadowMapSize) {
        depthTexture.reallocate(1, shadowMapSize, shadowMapSize, GL_DEPTH_COMPONENT32);
        framebuffer.attach(GL_DEPTH_ATTACHMENT, depthTexture, 0);
        framebuffer.ensureComplete();
    }

    public void terminate() {
        depthTexture.release();
        framebuffer.release();
    }
}
