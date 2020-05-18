package naitsirc98.beryl.graphics.opengl.skyboxpbr;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.swapchain.GLFramebuffer;
import naitsirc98.beryl.graphics.opengl.swapchain.GLRenderbuffer;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.meshes.MeshManager;
import naitsirc98.beryl.meshes.vertices.VertexLayouts;
import naitsirc98.beryl.resources.ManagedResource;
import naitsirc98.beryl.resources.Resource;
import naitsirc98.beryl.scenes.environment.skybox.SkyboxHelper;
import naitsirc98.beryl.scenes.environment.skybox.pbr.SkyboxPBRTextureFactory;

import java.nio.file.Path;

import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.meshes.PrimitiveMeshNames.QUAD_MESH_NAME;
import static naitsirc98.beryl.util.handles.LongHandle.NULL;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT16;
import static org.lwjgl.opengl.GL30C.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30C.GL_RG16F;

public class GLSkyboxPBRTextureFactory extends ManagedResource implements SkyboxPBRTextureFactory {

    public static final Path BRDF_VERTEX_SHADER_PATH = BerylFiles.getPath("shaders/pbr/brdf.vert");
    public static final Path BRDF_FRAGMENT_SHADER_PATH = BerylFiles.getPath("shaders/pbr/brdf.frag");

    private static final int QUAD_INDEX_COUNT = 6;


    private GLFramebuffer framebuffer;
    private GLShaderProgram brdfShader;
    private GLVertexArray quadVAO;
    private GLBuffer quadVertexBuffer;
    private GLBuffer quadIndexBuffer;

    public GLSkyboxPBRTextureFactory() {

    }

    @Override
    protected void free() {
        Resource.release(framebuffer);
        Resource.release(quadVAO);
        Resource.release(quadVertexBuffer);
        Resource.release(quadIndexBuffer);
    }

    @Override
    public Texture2D createBRDFTexture(int size) {

        GLTexture2D brdf = createNewBRDFTexture(size);

        bakeBRDFTexture(brdf, size);

        return brdf;
    }

    @Override
    public Cubemap createIrradianceMap(Cubemap environmentMap, int size) {
        return null;
    }

    @Override
    public Texture2D createPrefilterMap(Cubemap environmentMap, int size) {
        return null;
    }

    private void bakeBRDFTexture(GLTexture2D brdfTexture, int size) {

        prepareFramebufferForBRDF(brdfTexture);

        framebuffer().bind();

        glViewport(0, 0, size, size);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        GLShaderProgram shader = brdfShader();
        GLVertexArray quadVAO = quadVAO();

        shader.bind();
        quadVAO.bind();

        glDrawElements(GL_TRIANGLES, QUAD_INDEX_COUNT, GL_UNSIGNED_INT, NULL);

        quadVAO.unbind();
        shader.unbind();
    }

    private void prepareFramebufferForBRDF(GLTexture2D brdfTexture) {

        framebuffer().attach(GL_COLOR_ATTACHMENT0, brdfTexture, 0);

        framebuffer().ensureComplete();
    }

    private GLTexture2D createNewBRDFTexture(int size) {

        GLTexture2D brdf = new GLTexture2D();

        brdf.allocate(1, size, size, GL_RG16F);

        SkyboxHelper.setSkyboxTextureSamplerParameters(brdf);

        return brdf;
    }

    private GLShaderProgram brdfShader() {
        if(brdfShader == null) {
            brdfShader = createBRDFShaderProgram();
        }
        return brdfShader;
    }

    private GLShaderProgram createBRDFShaderProgram() {
        return new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(BRDF_VERTEX_SHADER_PATH))
                .attach(new GLShader(FRAGMENT_STAGE).source(BRDF_FRAGMENT_SHADER_PATH))
                .link();
    }

    private GLVertexArray quadVAO() {
        if(quadVAO == null) {
            quadVAO = createQuadVAO();
        }
        return quadVAO;
    }

    private GLVertexArray createQuadVAO() {

        Mesh quadMesh = MeshManager.get().get(QUAD_MESH_NAME);

        GLVertexArray vertexArray = new GLVertexArray();

        quadVertexBuffer = new GLBuffer("Quad VertexBuffer");
        quadVertexBuffer.data(quadMesh.vertexData());
        vertexArray.addVertexBuffer(0, VertexLayouts.VERTEX_LAYOUT_3D.attributeList(0), quadVertexBuffer);

        quadIndexBuffer = new GLBuffer("Quad IndexBuffer");
        quadIndexBuffer.data(quadMesh.indexData());
        vertexArray.setIndexBuffer(quadIndexBuffer);

        return vertexArray;
    }

    private GLFramebuffer framebuffer() {
        if(framebuffer == null) {
            framebuffer = createFramebuffer();
        }
        return framebuffer;
    }

    private GLFramebuffer createFramebuffer() {

        GLFramebuffer framebuffer = new GLFramebuffer();

        framebuffer.freeAttachmentsOnRelease(true);

        return framebuffer;
    }

    private GLRenderbuffer createDepthBuffer(int size) {

        GLRenderbuffer depthBuffer = new GLRenderbuffer();

        depthBuffer.storage(size, size, GL_DEPTH_COMPONENT16);

        return depthBuffer;
    }
}
