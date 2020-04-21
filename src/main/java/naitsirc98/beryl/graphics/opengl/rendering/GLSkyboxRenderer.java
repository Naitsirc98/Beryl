package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.textures.GLCubemap;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.graphics.rendering.renderers.SkyboxRenderer;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.meshes.models.StaticModelLoader;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.SceneEnvironment;
import naitsirc98.beryl.scenes.Skybox;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.util.handles.LongHandle.NULL;
import static naitsirc98.beryl.util.types.DataType.MATRIX4_SIZEOF;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;
import static org.lwjgl.system.MemoryStack.stackPush;

public class GLSkyboxRenderer implements SkyboxRenderer {

    private static final int MATRICES_BUFFER_SIZE = MATRIX4_SIZEOF * 2;
    private static final int PROJECTION_MATRIX_OFFSET = 0;
    private static final int VIEW_MATRIX_OFFSET = MATRIX4_SIZEOF;

    private static final int SKYBOX_INDEX_COUNT = 36;

    private GLShaderProgram shader;

    private GLVertexArray vertexArray;
    private GLBuffer vertexBuffer;
    private GLBuffer indexBuffer;

    private GLBuffer matricesUniformBuffer;

    private Matrix4f viewMatrix;

    @Override
    public void init() {

        shader = new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(BerylFiles.getPath("shaders/skybox/skybox.vert")))
                .attach(new GLShader(FRAGMENT_STAGE).source(BerylFiles.getPath("shaders/skybox/skybox.frag")))
                .link();

        Mesh cubeMesh = StaticMesh.cube();

        vertexArray = new GLVertexArray();

        vertexBuffer = new GLBuffer();
        vertexBuffer.data(cubeMesh.vertexData());

        indexBuffer = new GLBuffer();
        indexBuffer.data(cubeMesh.indexData());

        vertexArray.addVertexBuffer(0, VertexLayout.VERTEX_LAYOUT_3D.attributeList(0), vertexBuffer);

        vertexArray.setIndexBuffer(indexBuffer);

        matricesUniformBuffer = new GLBuffer();
        matricesUniformBuffer.allocate(MATRICES_BUFFER_SIZE);
        matricesUniformBuffer.mapMemory();

        viewMatrix = new Matrix4f();
    }

    @Override
    public void terminate() {
        vertexArray.release();
        vertexBuffer.release();
        indexBuffer.release();
    }

    public GLShaderProgram shader() {
        return shader;
    }

    @Override
    public void render(Scene scene) {

        final SceneEnvironment environment = scene.environment();
        final Skybox skybox = environment.skybox();

        if(skybox == null) {
            return;
        }

        final GLCubemap skyboxTexture1 = skybox.texture1();
        final GLCubemap skyboxTexture2 = skybox.texture2();
        final float textureBlendFactor = skybox.textureBlendFactor();

        updateMatrices(scene);

        glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glDisable(GL_CULL_FACE);

        shader.bind();

        matricesUniformBuffer.bind(GL_UNIFORM_BUFFER, 0);

        shader.uniformColorRGBA("u_FogColor", environment.fog().color());

        if(skyboxTexture1 != null) {
            shader.uniformSampler("u_SkyboxTexture1", skyboxTexture1, 0);
        }

        if(skyboxTexture2 != null) {
            shader.uniformSampler("u_SkyboxTexture2", skyboxTexture2, 1);
        }

        shader.uniformFloat("u_TextureBlendFactor", textureBlendFactor);

        vertexArray.bind();

        glDrawElements(GL_TRIANGLES, SKYBOX_INDEX_COUNT, GL_UNSIGNED_INT, NULL);

        if(skyboxTexture1 != null) {
            skyboxTexture1.unbind(0);
        }

        if(skyboxTexture2 != null) {
            skyboxTexture2.unbind(1);
        }
    }

    private void updateMatrices(Scene scene) {

        try(MemoryStack stack = stackPush()) {

            ByteBuffer buffer = stack.calloc(MATRICES_BUFFER_SIZE);

            viewMatrix.set(scene.camera().viewMatrix()).rotateY(scene.environment().skybox().rotation());

            scene.camera().projectionMatrix().get(PROJECTION_MATRIX_OFFSET, buffer);
            viewMatrix.get(VIEW_MATRIX_OFFSET, buffer);

            matricesUniformBuffer.copy(0, buffer);
        }
    }

}
