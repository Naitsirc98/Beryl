package naitsirc98.beryl.graphics.opengl.skyboxpbr;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.opengl.buffers.GLBuffer;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.swapchain.GLFramebuffer;
import naitsirc98.beryl.graphics.opengl.swapchain.GLRenderbuffer;
import naitsirc98.beryl.graphics.opengl.textures.GLCubemap;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexArray;
import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.meshes.MeshManager;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.resources.ManagedResource;
import naitsirc98.beryl.resources.Resource;
import naitsirc98.beryl.scenes.environment.skybox.SkyboxHelper;
import naitsirc98.beryl.scenes.environment.skybox.pbr.SkyboxPBRTextureFactory;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.pow;
import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.meshes.PrimitiveMeshNames.QUAD_MESH_NAME;
import static naitsirc98.beryl.meshes.vertices.VertexLayouts.VERTEX_LAYOUT_3D;
import static naitsirc98.beryl.util.Maths.radians;
import static naitsirc98.beryl.util.handles.LongHandle.NULL;
import static org.lwjgl.opengl.GL40.*;

public class GLSkyboxPBRTextureFactory extends ManagedResource implements SkyboxPBRTextureFactory {

    public static final Path BRDF_VERTEX_SHADER_PATH = BerylFiles.getPath("shaders/skybox/brdf.vert");
    public static final Path BRDF_FRAGMENT_SHADER_PATH = BerylFiles.getPath("shaders/skybox/brdf.frag");

    private static final Path IRRADIANCE_VERTEX_SHADER_PATH = BerylFiles.getPath("shaders/skybox/irradiance_map.vert");
    private static final Path IRRADIANCE_FRAGMENT_SHADER_PATH = BerylFiles.getPath("shaders/skybox/irradiance_map.frag");

    private static final Path PREFILTER_VERTEX_SHADER_PATH = BerylFiles.getPath("shaders/skybox/prefilter_map.vert");
    private static final Path PREFILTER_FRAGMENT_SHADER_PATH = BerylFiles.getPath("shaders/skybox/prefilter_map.frag");

    private static final Path ENVIRONMENT_VERTEX_SHADER_PATH = BerylFiles.getPath("shaders/skybox/equirect_to_cubemap.vert");
    private static final Path ENVIRONMENT_FRAGMENT_SHADER_PATH = BerylFiles.getPath("shaders/skybox/equirect_to_cubemap.frag");

    private static final int QUAD_INDEX_COUNT = 6;
    private static final int CUBE_INDEX_COUNT = 36;

    public static final String ENVIRONMENT_MAP_UNIFORM_NAME = "u_EnvironmentMap";
    public static final String EQUIRECTANGULAR_MAP_UNIFORM_NAME = "u_EquirectangularMap";
    public static final String PROJECTION_VIEW_MATRIX_UNIFORM_NAME = "u_ProjectionViewMatrix";


    // Framebuffer
    private GLFramebuffer framebuffer;
    // Shaders
    private GLShaderProgram environmentMapShader;
    private GLShaderProgram irradianceShader;
    private GLShaderProgram prefilterShader;
    private GLShaderProgram brdfShader;
    // Vertex data
    private GLVertexArray quadVAO;
    private GLBuffer quadVertexBuffer;
    private GLBuffer quadIndexBuffer;
    private GLVertexArray cubeVAO;
    private GLBuffer cubeVertexBuffer;
    private GLBuffer cubeIndexBuffer;
    // Depth buffers
    @Deprecated
    private final Map<Integer, GLRenderbuffer> depthBuffers;

    public GLSkyboxPBRTextureFactory() {
        depthBuffers = new HashMap<>();
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

        depthBuffers.values().forEach(GLRenderbuffer::release);
        depthBuffers.clear();
    }

    @Override
    public Texture2D createBRDFTexture(int size) {

        GLTexture2D brdf = createNewBRDFTexture(size);

        bakeBRDFTexture(brdf, size);

        return brdf;
    }

    @Override
    public Cubemap createEnvironmentMap(String hdrTexturePath, int size) {

        GLTexture2D hdrTexture = (GLTexture2D) GraphicsFactory.get().newTexture2DFloat(hdrTexturePath, PixelFormat.RGB16F);

        SkyboxHelper.setSkyboxTextureSamplerParameters(hdrTexture);

        GLCubemap environmentTexture = createNewEnvironmentTexture(size);

        bakeEnvironmentMap(hdrTexture, environmentTexture, size);

        return environmentTexture;
    }

    @Override
    public Cubemap createIrradianceMap(Cubemap environmentMap, int size) {

        GLCubemap irradianceTexture = createNewIrradianceTexture(size);

        bakeIrradianceMap((GLCubemap) environmentMap, irradianceTexture, size);

        return irradianceTexture;
    }

    @Override
    public Cubemap createPrefilterMap(Cubemap environmentMap, int size, float maxLOD) {

        final int mipLevels = Math.round(maxLOD) + 1;

        Cubemap prefilterTexture = createNewPrefilterTexture(size, mipLevels);

        // Run a quasi monte-carlo simulation on the environment lighting to create a prefilter (cube)map.
        bakePrefilterMap(environmentMap, prefilterTexture, size, mipLevels);

        return prefilterTexture;
    }

    private void bakeEnvironmentMap(GLTexture2D equirectangularTexture, GLCubemap environmentMap, int size) {

        GLShaderProgram shader = environmentMapShader();

        shader.bind();

        shader.uniformSampler(EQUIRECTANGULAR_MAP_UNIFORM_NAME, equirectangularTexture, 0);

        renderCubemap(environmentMap, shader, size, 0);

        shader.unbind();

        environmentMap.generateMipmaps();
    }

    private void bakeIrradianceMap(GLCubemap environmentMap, GLCubemap irradianceTexture, int size) {

        GLShaderProgram shader = irradianceShader();

        shader.bind();

        shader.uniformSampler(ENVIRONMENT_MAP_UNIFORM_NAME, environmentMap, 0);

        renderCubemap(irradianceTexture, shader, size, 0);

        shader.unbind();

        irradianceTexture.generateMipmaps();
    }

    private void bakePrefilterMap(Cubemap environmentMap, Cubemap prefilterTexture, int size, int mipLevels) {

        GLShaderProgram shader = prefilterShader();

        shader.bind();

        shader.uniformSampler(ENVIRONMENT_MAP_UNIFORM_NAME, (GLTexture) environmentMap, 0);

        shader.uniformInt("u_Resolution", environmentMap.width());

        final int minMipLevel = mipLevels - 1;

        for(int mipLevel = 0;mipLevel <= minMipLevel;mipLevel++) {

            final int mipLevelSize = (int)(size * pow(0.5f, mipLevel));

            final float roughness = (float)mipLevel / (float)minMipLevel;

            shader.uniformFloat("u_Roughness", roughness);

            renderCubemap((GLCubemap) prefilterTexture, shader, mipLevelSize, mipLevel);
        }

        // Do not generate mipmaps after render!
        // TODO
        // prefilterTexture.generateMipmaps();

        shader.unbind();
    }

    private void bakeBRDFTexture(GLTexture2D brdfTexture, int size) {

        framebuffer().attach(GL_COLOR_ATTACHMENT0, brdfTexture, 0);
        framebuffer().ensureComplete();

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

        framebuffer().unbind();
    }

    private void renderCubemap(GLCubemap cubemap, GLShaderProgram shader, int size, int mipmapLevel) {

        Cubemap.Face[] faces = Cubemap.Face.values();

        Matrix4fc projectionMatrix = getProjectionMatrix();

        Matrix4f[] viewMatrices = getViewMatrices();

        GLVertexArray cubeVAO = cubeVAO();

        cubeVAO.bind();

        // cubemap.bind();

        framebuffer().bind();

        glDisable(GL_DEPTH_TEST);
        glViewport(0, 0, size, size);

        int f = GL_TEXTURE_CUBE_MAP_POSITIVE_X;

        for(int i = 0;i < faces.length;i++) {

            Matrix4f viewMatrix = viewMatrices[i];

            Matrix4f projectionViewMatrix = projectionMatrix.mul(viewMatrix, new Matrix4f());

            shader.uniformMatrix4f(PROJECTION_VIEW_MATRIX_UNIFORM_NAME, false, projectionViewMatrix);

            framebuffer().bind();

            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, f, cubemap.handle(), mipmapLevel);

            f++;

            // framebuffer().attach(GL_COLOR_ATTACHMENT0, cubemap, faces[i], mipmapLevel);
            // framebuffer().ensureComplete();

            glDrawElements(GL_TRIANGLES, CUBE_INDEX_COUNT, GL_UNSIGNED_INT, NULL);

        }
        glFinish();

        // framebuffer().detach(GL_COLOR_ATTACHMENT0);

        framebuffer().unbind();

        // cubemap.unbind();

        cubeVAO.unbind();
    }

    private Matrix4f[] getViewMatrices() {
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
        return new Matrix4f().perspective(radians(90.0f), 1.0f, 0.1f, 100.0f);
    }

    private GLCubemap createNewEnvironmentTexture(int size) {

        GLCubemap environmentTexture = new GLCubemap();

        environmentTexture.allocate(4, size, size, PixelFormat.RGB16F);

        SkyboxHelper.setSkyboxTextureSamplerParameters(environmentTexture);

        return environmentTexture;
    }

    private GLCubemap createNewIrradianceTexture(int size) {

        GLCubemap irradianceTexture = new GLCubemap();

        irradianceTexture.allocate(4, size, size, PixelFormat.RGB16F);

        SkyboxHelper.setSkyboxTextureSamplerParameters(irradianceTexture);

        irradianceTexture.generateMipmaps();

        return irradianceTexture;
    }

    private Cubemap createNewPrefilterTexture(int size, int mipLevels) {

        GLCubemap prefilterTexture = new GLCubemap();

        prefilterTexture.allocate(mipLevels, size, size, PixelFormat.RGB16F);

        SkyboxHelper.setSkyboxTextureSamplerParameters(prefilterTexture);

        prefilterTexture.generateMipmaps();

        return prefilterTexture;
    }

    private GLTexture2D createNewBRDFTexture(int size) {

        GLTexture2D brdf = new GLTexture2D();

        brdf.allocate(1, size, size, GL_RG16F);

        return SkyboxHelper.setSkyboxTextureSamplerParameters(brdf);
    }

    private GLShaderProgram environmentMapShader() {
        if(environmentMapShader == null) {
            environmentMapShader = createEnvironmentMapShader();
        }
        return environmentMapShader;
    }

    private GLShaderProgram createEnvironmentMapShader() {
        return new GLShaderProgram("Equirectangular to cubemap Shader")
                .attach(new GLShader(VERTEX_STAGE).source(ENVIRONMENT_VERTEX_SHADER_PATH))
                .attach(new GLShader(FRAGMENT_STAGE).source(ENVIRONMENT_FRAGMENT_SHADER_PATH))
                .link();
    }

    private GLShaderProgram irradianceShader() {
        if(irradianceShader == null) {
            irradianceShader = createIrradianceShader();
        }
        return irradianceShader;
    }

    private GLShaderProgram createIrradianceShader() {
        return new GLShaderProgram("Irradiance Shader")
                .attach(new GLShader(VERTEX_STAGE).source(IRRADIANCE_VERTEX_SHADER_PATH))
                .attach(new GLShader(FRAGMENT_STAGE).source(IRRADIANCE_FRAGMENT_SHADER_PATH))
                .link();
    }

    private GLShaderProgram prefilterShader() {
        if(prefilterShader == null) {
            prefilterShader = createPrefilterShader();
        }
        return prefilterShader;
    }

    private GLShaderProgram createPrefilterShader() {
        return new GLShaderProgram("Prefilter Shader")
                .attach(new GLShader(VERTEX_STAGE).source(PREFILTER_VERTEX_SHADER_PATH))
                .attach(new GLShader(FRAGMENT_STAGE).source(PREFILTER_FRAGMENT_SHADER_PATH))
                .link();
    }

    private GLShaderProgram brdfShader() {
        if(brdfShader == null) {
            brdfShader = createBRDFShaderProgram();
        }
        return brdfShader;
    }

    private GLShaderProgram createBRDFShaderProgram() {
        return new GLShaderProgram("BRDF Shader")
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
        Mesh cubeMesh = StaticMesh.cube();
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

    private GLRenderbuffer getDepthBuffer(int size) {

        if(depthBuffers.containsKey(size)) {
            return depthBuffers.get(size);
        }

        GLRenderbuffer depthBuffer = new GLRenderbuffer();

        depthBuffer.storage(size, size, GL_DEPTH_COMPONENT16);

        depthBuffers.put(size, depthBuffer);

        return depthBuffer;
    }
}
