package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.examples.app1.App1;
import naitsirc98.beryl.graphics.opengl.buffers.GLUniformBuffer;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.swapchain.GLFramebuffer;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexData;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.lights.Light;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.camera.Camera;
import naitsirc98.beryl.scenes.components.lights.LightSource;
import naitsirc98.beryl.scenes.components.meshes.MeshView;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.util.handles.LongHandle.NULL;
import static org.lwjgl.opengl.ARBFramebufferObject.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11C.glDrawElements;
import static org.lwjgl.opengl.GL14C.GL_DEPTH_COMPONENT24;

public class GLCascadedShadowMaps extends RenderingPath {

    private static final int SHADOW_CASCADES_COUNT = 3;

    private static final int DEPTH_MAP_SIZE = 2048;

    private static final Path DEPTH_VERTEX_SHADER_PATH = BerylFiles.getPath("shaders/depth/directional_depth.vert");
    private static final Path DEPTH_FRAGMENT_SHADER_PATH = BerylFiles.getPath("shaders/depth/depth.frag");

    private static final Path VERTEX_SHADER_PATH = BerylFiles.getPath("shaders/depth/shadows.vert");
    private static final Path FRAGMENT_SHADER_PATH = BerylFiles.getPath("shaders/depth/shadows.frag");

    /*
      Divide the view frustum into n splits.
      While rendering the depth map, for each split:
        Calculate light view and projection matrices.
        Render the scene from light’s perspective into a separate depth map
      While rendering the scene:
        Use the depths maps calculated above.
        Determine the split that the fragment to be drawn belongs to.
        Calculate shadow factor as in shadow maps.
     */

    private ShadowCascade[] shadowCascades;
    private GLFramebuffer depthFramebuffer;
    private GLTexture2D[] shadowMaps;
    private GLShaderProgram depthShader;
    private GLShaderProgram shader;
    private GLUniformBuffer lightsUniformBuffer;

    @Override
    protected void init() {

        shadowCascades = new ShadowCascade[SHADOW_CASCADES_COUNT];

        for(int i = 0;i < SHADOW_CASCADES_COUNT;i++) {
            shadowCascades[i] = new ShadowCascade();
        }

        shadowMaps = new GLTexture2D[SHADOW_CASCADES_COUNT];

        for(int i = 0;i < SHADOW_CASCADES_COUNT;i++) {
            GLTexture2D depthMap = shadowMaps[i] = new GLTexture2D();
            depthMap.allocate(1, DEPTH_MAP_SIZE, DEPTH_MAP_SIZE, GL_DEPTH_COMPONENT24);
        }

        depthFramebuffer = new GLFramebuffer();

        depthFramebuffer.drawBuffer(GL_NONE);
        depthFramebuffer.readBuffer(GL_NONE);

        depthShader = new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(DEPTH_VERTEX_SHADER_PATH).compile())
                .attach(new GLShader(FRAGMENT_STAGE).source(DEPTH_FRAGMENT_SHADER_PATH).compile())
                .link();

        shader = new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(VERTEX_SHADER_PATH).compile())
                .attach(new GLShader(FRAGMENT_STAGE).source(FRAGMENT_SHADER_PATH).compile())
                .link();

        lightsUniformBuffer = new GLUniformBuffer("LightsUniformBuffer", shader, 0);
        lightsUniformBuffer.allocate(Light.SIZEOF);
    }

    @Override
    protected void terminate() {
        Arrays.stream(shadowMaps).forEach(Texture2D::release);
    }

    public void render(Camera camera, Scene scene) {

        List<MeshView> meshes = scene.meshViews();

        DirectionalLight light = null;

        for(LightSource lightSource : scene.lightSources()) {
            if(lightSource.light() instanceof DirectionalLight) {
                light = lightSource.light();
                break;
            }
        }

        final float[] cascadeRanges = {
                camera.nearPlane(),
                camera.farPlane() / 20.0f,
                camera.farPlane() / 10.0f,
                camera.farPlane()
        };

        renderDepth(light, camera, meshes, cascadeRanges);

        renderScene(light, camera, meshes, cascadeRanges);
    }

    private void renderScene(DirectionalLight light, Camera camera, List<MeshView> meshes, float[] cascadeRanges) {

        // Render scene

        GLFramebuffer.bindDefault();

        glEnable(GL_DEPTH_TEST);
        glViewport(0, 0, Window.get().width(), Window.get().height());
        glClearColor(camera.clearColor().red(), camera.clearColor().green(), camera.clearColor().blue(), camera.clearColor().alpha());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        // glEnable(GL_CULL_FACE);

        shader.use();

        lightsUniformBuffer.bind();

        try(MemoryStack stack = MemoryStack.stackPush()) {

            ByteBuffer lightBuffer = stack.malloc(Light.SIZEOF);

            lightsUniformBuffer.update(0, light.get(0, lightBuffer));

            shader.uniformVector4f("u_CameraPosition", camera.transform().position());

            FloatBuffer buffer = stack.mallocFloat(16);

            for(int i = 0;i < shadowCascades.length;i++) {
                Matrix4f lmv = shadowCascades[i].lightProjectionMatrix().mul(shadowCascades[i].lightViewMatrix(), new Matrix4f());
                shader.uniformMatrix4f("u_LightMV["+i+"]", false, lmv.get(buffer));
            }

            shader.uniformMatrix4f("u_Projection", false, camera.projectionMatrix().get(buffer));
            shader.uniformMatrix4f("u_View", false, camera.viewMatrix().get(buffer));

            for(int i = 0;i < shadowCascades.length;i++) {
                shader.uniformSampler("u_ShadowMap["+i+"]", shadowMaps[i], i);
            }

            for(int i = 0;i < cascadeRanges.length;i++) {
                shader.uniformFloat("u_CascadeFarPlanes["+i+"]", cascadeRanges[i]);
            }

            Matrix4f mvp = new Matrix4f();

            for(MeshView meshView : meshes) {

                for(Mesh mesh : meshView) {

                    /*

                    shader.uniformMatrix4f("u_Model", false, meshView.modelMatrix().get(buffer));
                    shader.uniformMatrix4f("u_NormalMatrix", false, meshView.normalMatrix().get(buffer));
                    shader.uniformMatrix4f("u_MVP", false, camera.projectionViewMatrix().mul(meshView.modelMatrix(), mvp).get(buffer));

                    GLVertexData vertexData = mesh.vertexData();

                    vertexData.bind();

                    if(vertexData.indexCount() > 0) {
                        glDrawElements(GL_TRIANGLES, vertexData.indexCount(), GL_UNSIGNED_INT, NULL);
                    } else {
                        glDrawArrays(GL_TRIANGLES, 0, vertexData.vertexCount());
                    }

                     */

                    shader.uniformMatrix4f("u_Model", false, new Matrix4f().get(buffer));

                    GLVertexData vertexData = App1.quadMesh.vertexData();

                    vertexData.bind();

                    if(vertexData.indexCount() > 0) {
                        glDrawElements(GL_TRIANGLES, vertexData.indexCount(), GL_UNSIGNED_INT, NULL);
                    } else {
                        glDrawArrays(GL_TRIANGLES, 0, vertexData.vertexCount());
                    }

                    break;
                }
            }

        }
    }

    private void renderDepth(DirectionalLight light, Camera camera, List<MeshView> meshes, float[] cascadeRanges) {

        depthShader.use();

        Matrix4f depthMVP = new Matrix4f();

        try(MemoryStack stack = MemoryStack.stackPush()) {

            FloatBuffer buffer = stack.mallocFloat(16);

            for(int i = 0;i < SHADOW_CASCADES_COUNT;i++) {

                // Render from lights perspective. Save the result in shadowMaps[i]

                depthFramebuffer.attach(GL_DEPTH_ATTACHMENT, shadowMaps[i], 0);
                depthFramebuffer.ensureComplete();
                depthFramebuffer.bind();

                glEnable(GL_DEPTH_TEST);
                glViewport(0, 0, DEPTH_MAP_SIZE, DEPTH_MAP_SIZE);
                glClearColor(0, 0, 0, 0);
                glClear(GL_DEPTH_BUFFER_BIT);

                ShadowCascade shadowCascade = shadowCascades[i];

                shadowCascade.update(camera, cascadeRanges[i], cascadeRanges[i+1], light);

                for(MeshView meshView : meshes) {

                    if(!meshView.castShadows()) {
                        continue;
                    }

                    shadowCascade.lightProjectionMatrix().mul(shadowCascade.lightViewMatrix(), depthMVP);
                    depthMVP.mul(meshView.modelMatrix());

                    for(Mesh mesh : meshView) {

                        depthShader.uniformMatrix4f("u_DepthMVP", false, depthMVP.get(buffer));

                        GLVertexData vertexData = mesh.vertexData();

                        vertexData.bind();

                        if(vertexData.indexCount() > 0) {
                            glDrawElements(GL_TRIANGLES, vertexData.indexCount(), GL_UNSIGNED_INT, NULL);
                        } else {
                            glDrawArrays(GL_TRIANGLES, 0, vertexData.vertexCount());
                        }
                    }
                }

            }
        }

    }


}
