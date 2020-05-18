package naitsirc98.beryl.graphics.opengl.skyboxpbr;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.swapchain.GLFramebuffer;
import naitsirc98.beryl.graphics.opengl.swapchain.GLRenderbuffer;
import naitsirc98.beryl.graphics.opengl.textures.GLCubemap;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.meshes.MeshManager;
import naitsirc98.beryl.resources.ManagedResource;
import naitsirc98.beryl.resources.Resource;
import naitsirc98.beryl.scenes.environment.skybox.SkyboxHelper;
import naitsirc98.beryl.scenes.environment.skybox.pbr.SkyboxPBRTextureFactory;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.nio.file.Path;

import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.meshes.PrimitiveMeshNames.CUBE_MESH_NAME;
import static naitsirc98.beryl.meshes.PrimitiveMeshNames.QUAD_MESH_NAME;
import static naitsirc98.beryl.meshes.vertices.VertexLayouts.VERTEX_LAYOUT_3D;
import static naitsirc98.beryl.util.handles.LongHandle.NULL;
import static org.lwjgl.opengl.GL40.*;

public class GLSkyboxPBRTextureFactory extends ManagedResource implements SkyboxPBRTextureFactory {

    public static final Path BRDF_VERTEX_SHADER_PATH = BerylFiles.getPath("shaders/skybox/brdf.vert");
    public static final Path BRDF_FRAGMENT_SHADER_PATH = BerylFiles.getPath("shaders/skybox/brdf.frag");

    private static final Path IRRADIANCE_VERTEX_SHADER_PATH = BerylFiles.getPath("shaders/skybox/irradiance_map.vert");
    private static final Path IRRADIANCE_FRAGMENT_SHADER_PATH = BerylFiles.getPath("shaders/skybox/irradiance_map.frag");

    private static final int QUAD_INDEX_COUNT = 6;
    private static final int CUBE_INDEX_COUNT = 36;


    // Framebuffer
    private GLFramebuffer framebuffer;
    // Shaders
    private GLShaderProgram brdfShader;
    private GLShaderProgram irradianceShader;
    // Vertex data
    private GLVertexArray quadVAO;
    private GLBuffer quadVertexBuffer;
    private GLBuffer quadIndexBuffer;
    private GLVertexArray cubeVAO;
    private GLBuffer cubeVertexBuffer;
    private GLBuffer cubeIndexBuffer;

    public GLSkyboxPBRTextureFactory() {

    }

    @Override
    protected void free() {

        Resource.release(framebuffer);
        Resource.release(brdfShader);
        Resource.release(irradianceShader);
        Resource.release(quadVAO);
        Resource.release(quadVertexBuffer);
        Resource.release(quadIndexBuffer);
        Resource.release(cubeVAO);
        Resource.release(cubeVertexBuffer);
        Resource.release(cubeIndexBuffer);
    }

    @Override
    public Texture2D createBRDFTexture(int size) {

        GLTexture2D brdf = createNewBRDFTexture(size);

        bakeBRDFTexture(brdf, size);

        return brdf;
    }

    @Override
    public Cubemap createIrradianceMap(Cubemap environmentMap, int size) {

        GLCubemap irradianceTexture = createNewIrradianceTexture(size);

        bakeIrradianceMap((GLCubemap) environmentMap, irradianceTexture, size);

        return irradianceTexture;
    }

    @Override
    public Texture2D createPrefilterMap(Cubemap environmentMap, int size) {
        return null;
    }

    private void bakeIrradianceMap(GLCubemap environmentMap, GLCubemap irradianceTexture, int size) {

        framebuffer().bind();

        GLShaderProgram shader = irradianceShader();

        shader.bind();

        shader.uniformSampler("u_EnvironmentMap", environmentMap, 0);

        renderCubemap(irradianceTexture, shader, size, 0);

        shader.unbind();

        framebuffer().detach(GL_DEPTH_ATTACHMENT);
    }

    private void renderCubemap(GLCubemap cubemap, GLShaderProgram shader, int size, int mipmapLevel) {

        Cubemap.Face[] faces = Cubemap.Face.values();

        Matrix4fc projectionMatrix = getProjectionMatrix();

        Matrix4f[] viewMatrices = getCaptureViewMatrices();

        GLVertexArray cubeVAO = cubeVAO();

        cubeVAO.bind();

        framebuffer().bind();

        for(int i = 0;i < faces.length;i++) {

            Matrix4f projectionViewMatrix = viewMatrices[i];
            projectionMatrix.mul(projectionViewMatrix, projectionViewMatrix);

            shader.uniformMatrix4f("u_ProjectionViewMatrix", false, projectionViewMatrix);

            framebuffer().attach(GL_COLOR_ATTACHMENT0, cubemap, faces[i], mipmapLevel);
            framebuffer().drawBuffer(GL_COLOR_ATTACHMENT0);
            framebuffer().ensureComplete();

            glEnable(GL_DEPTH_TEST);
            glViewport(0, 0, size, size);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glDrawElements(GL_TRIANGLES, CUBE_INDEX_COUNT, GL_UNSIGNED_INT, NULL);
        }

        cubeVAO.unbind();
    }

    private Matrix4f[] getCaptureViewMatrices() {
        return new Matrix4f[] {
                new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f, 1.0f,  0.0f,  0.0f, 0.0f, -1.0f,  0.0f),
                new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f,-1.0f,  0.0f,  0.0f, 0.0f, -1.0f,  0.0f),
                new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f, 0.0f,  1.0f,  0.0f, 0.0f,  0.0f,  1.0f),
                new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f, 0.0f, -1.0f,  0.0f, 0.0f,  0.0f, -1.0f),
                new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f, 0.0f,  0.0f,  1.0f, 0.0f, -1.0f,  0.0f),
                new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f,  0.0f, -1.0f, 0.0f)
        };
    }

    private Matrix4fc getProjectionMatrix() {
        return new Matrix4f().perspective((float)Math.toRadians(90), 1.0f, 0.1f, 10.0f);
    }

    private GLCubemap createNewIrradianceTexture(int size) {

        GLCubemap irradianceTexture = new GLCubemap();

        irradianceTexture.allocate(1, size, size, GL_RGB16F);

        return SkyboxHelper.setSkyboxTextureSamplerParameters(irradianceTexture);
    }

    private void bakeBRDFTexture(GLTexture2D brdfTexture, int size) {

        prepareFramebufferForBRDF(brdfTexture);

        framebuffer().bind();

        glDisable(GL_DEPTH_TEST);
        glViewport(0, 0, size, size);
        glClear(GL_COLOR_BUFFER_BIT);

        GLShaderProgram shader = brdfShader();
        GLVertexArray quadVAO = quadVAO();

        shader.bind();
        quadVAO.bind();

        glDrawElements(GL_TRIANGLES, QUAD_INDEX_COUNT, GL_UNSIGNED_INT, NULL);

        quadVAO.unbind();
        shader.unbind();

        framebuffer().detach(GL_COLOR_ATTACHMENT0);
    }

    private void prepareFramebufferForBRDF(GLTexture2D brdfTexture) {

        framebuffer().attach(GL_COLOR_ATTACHMENT0, brdfTexture, 0);

        framebuffer().drawBuffer(GL_COLOR_ATTACHMENT0);

        framebuffer().ensureComplete();
    }

    private GLTexture2D createNewBRDFTexture(int size) {

        GLTexture2D brdf = new GLTexture2D();

        brdf.allocate(1, size, size, GL_RG16F);

        SkyboxHelper.setSkyboxTextureSamplerParameters(brdf);

        return brdf;
    }

    private GLShaderProgram irradianceShader() {
        if(irradianceShader == null) {
            irradianceShader = createIrradianceShader();
        }
        return irradianceShader;
    }

    private GLShaderProgram createIrradianceShader() {
        return new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(IRRADIANCE_VERTEX_SHADER_PATH))
                .attach(new GLShader(FRAGMENT_STAGE).source(IRRADIANCE_FRAGMENT_SHADER_PATH))
                .link();
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

    private GLVertexArray cubeVAO() {
        if(cubeVAO == null) {
            cubeVAO = createCubeVAO();
        }
        return cubeVAO;
    }

    private GLVertexArray createCubeVAO() {
        Mesh cubeMesh = MeshManager.get().get(CUBE_MESH_NAME);
        cubeVertexBuffer = new GLBuffer("Cube VertexBuffer");
        cubeIndexBuffer = new GLBuffer("Cube IndexBuffer");
        return createVertexArray(cubeMesh, cubeVertexBuffer, cubeIndexBuffer);
    }

    private GLVertexArray quadVAO() {
        if(quadVAO == null) {
            quadVAO = createQuadVAO();
        }
        return quadVAO;
    }

    private GLVertexArray createQuadVAO() {
        Mesh quadMesh = MeshManager.get().get(QUAD_MESH_NAME);
        quadVertexBuffer = new GLBuffer("Quad VertexBuffer");
        quadIndexBuffer = new GLBuffer("Quad IndexBuffer");
        return createVertexArray(quadMesh, quadVertexBuffer, quadIndexBuffer);
    }

    private GLVertexArray createVertexArray(Mesh mesh, GLBuffer vertexBuffer, GLBuffer indexBuffer) {

        GLVertexArray vertexArray = new GLVertexArray();

        vertexBuffer.data(mesh.vertexData());
        vertexArray.addVertexBuffer(0, VERTEX_LAYOUT_3D.attributeList(0), vertexBuffer);

        indexBuffer.data(mesh.indexData());
        vertexArray.setIndexBuffer(indexBuffer);

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
