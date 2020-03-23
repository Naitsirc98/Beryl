package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.graphics.opengl.buffers.GLUniformBuffer;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexData;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.lights.Light;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.camera.Camera;
import naitsirc98.beryl.scenes.components.lights.LightSource;
import naitsirc98.beryl.scenes.components.meshes.MeshView;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;

import static java.lang.Math.min;
import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.INT32_SIZEOF;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class GLPhongRenderingPath extends RenderingPath {

    private static final Path VERTEX_SHADER_PATH;
    private static final Path FRAGMENT_SHADER_PATH;

    // MVP, ModelMatrix, NormalMatrix, CameraPosition
    private static final int MATRICES_UNIFORM_BUFFER_SIZE = (16 + 16 + 16 + 4) * FLOAT32_SIZEOF;
    private static final String MATRICES_UNIFORM_BUFFER_NAME = "MatricesUniformBuffer";
    private static final int MATRICES_UNIFORM_BUFFER_MVP_OFFSET = 0;
    private static final int MATRICES_UNIFORM_BUFFER_MODEL_MATRIX_OFFSET = 16 * FLOAT32_SIZEOF;
    private static final int MATRICES_UNIFORM_BUFFER_NORMAL_MATRIX_OFFSET = 32 * FLOAT32_SIZEOF;
    private static final int MATRICES_UNIFORM_BUFFER_CAMERA_POSITION_OFFSET = 48 * FLOAT32_SIZEOF;

    private static final int MATERIAL_UNIFORM_BUFFER_SIZE = PhongMaterial.SIZEOF;
    private static final String MATERIAL_UNIFORM_BUFFER_NAME = "MaterialUniformBuffer";

    private static final int LIGHTS_MAX_COUNT = 100;
    private static final int LIGHTS_UNIFORM_BUFFER_SIZE = LIGHTS_MAX_COUNT * Light.SIZEOF + INT32_SIZEOF;
    private static final String LIGHTS_UNIFORM_BUFFER_NAME = "LightsUniformBuffer";
    private static final int LIGHTS_UNIFORM_BUFFER_COUNT_OFFSET = LIGHTS_UNIFORM_BUFFER_SIZE - INT32_SIZEOF;

    private static final String UNIFORM_AMBIENT_MAP_NAME = "u_AmbientMap";
    private static final String UNIFORM_DIFFUSE_MAP_NAME = "u_DiffuseMap";
    private static final String UNIFORM_SPECULAR_MAP_NAME =  "u_SpecularMap";
    private static final String UNIFORM_EMISSIVE_MAP_NAME =  "u_EmissiveMap";

    static {

        Path vertexPath = null;
        Path fragmentPath = null;

        try {
            vertexPath = BerylFiles.getPath("shaders/phong/phong.vert");
            fragmentPath = BerylFiles.getPath("shaders/phong/phong.frag");
        } catch (Exception e) {
            Log.fatal("Failed to get shader files for RenderingPath", e);
        }

        VERTEX_SHADER_PATH = vertexPath;
        FRAGMENT_SHADER_PATH = fragmentPath;
    }

    private GLShaderProgram shader;

    private GLUniformBuffer matricesUniformBuffer;
    private GLUniformBuffer materialUniformBuffer;
    private GLUniformBuffer lightsUniformBuffer;

    private Matrix4f projectionViewMatrix;
    private GLVertexData lastVertexData;
    private PhongMaterial lastMaterial;

    private GLPhongRenderingPath() {

    }

    @Override
    protected void init() {

        shader = new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(VERTEX_SHADER_PATH).compile())
                .attach(new GLShader(FRAGMENT_STAGE).source(FRAGMENT_SHADER_PATH).compile())
                .link();

        matricesUniformBuffer = new GLUniformBuffer(MATRICES_UNIFORM_BUFFER_NAME, shader, 0);
        matricesUniformBuffer.allocate(MATRICES_UNIFORM_BUFFER_SIZE);

        materialUniformBuffer = new GLUniformBuffer(MATERIAL_UNIFORM_BUFFER_NAME, shader, 1);
        materialUniformBuffer.allocate(MATERIAL_UNIFORM_BUFFER_SIZE);

        lightsUniformBuffer = new GLUniformBuffer(LIGHTS_UNIFORM_BUFFER_NAME, shader, 2);
        lightsUniformBuffer.allocate(LIGHTS_UNIFORM_BUFFER_SIZE);

        projectionViewMatrix = new Matrix4f();
    }

    @Override
    protected void terminate() {

        shader.release();

        matricesUniformBuffer.release();
        materialUniformBuffer.release();
        lightsUniformBuffer.release();
    }

    @Override
    public void render(Camera camera, Scene scene) {

        glEnable(GL_DEPTH_TEST);
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        final GLShaderProgram shader = this.shader;
        final Matrix4fc projectionView = camera.projectionViewMatrix();
        final Matrix4f mvp = projectionViewMatrix;
        final GLUniformBuffer matricesUniformBuffer = this.matricesUniformBuffer;

        shader.use();

        matricesUniformBuffer.bind();
        lightsUniformBuffer.bind();
        materialUniformBuffer.bind();

        try(MemoryStack stack = stackPush()) {

            setLightsUniformBuffer(scene.lightSources(), stack);

            final ByteBuffer matricesBuffer = stack.malloc(MATRICES_UNIFORM_BUFFER_SIZE - 4  * FLOAT32_SIZEOF);

            matricesUniformBuffer.update(MATRICES_UNIFORM_BUFFER_CAMERA_POSITION_OFFSET,
                    camera.transform().position().get(stack.malloc(4 * FLOAT32_SIZEOF)));

            for(MeshView meshView : scene.meshViews()) {

                projectionView.mul(meshView.modelMatrix(), mvp).get(MATRICES_UNIFORM_BUFFER_MVP_OFFSET, matricesBuffer);
                meshView.modelMatrix().get(MATRICES_UNIFORM_BUFFER_MODEL_MATRIX_OFFSET, matricesBuffer);
                meshView.normalMatrix().get(MATRICES_UNIFORM_BUFFER_NORMAL_MATRIX_OFFSET, matricesBuffer);

                matricesUniformBuffer.update(0, matricesBuffer);

                for(Mesh mesh : meshView) {

                    final GLVertexData vertexData = mesh.vertexData();
                    final PhongMaterial material = meshView.material(mesh);

                    if(lastVertexData != vertexData) {
                        vertexData.bind();
                        lastVertexData = vertexData;
                    }

                    if(lastMaterial != material) {
                        setMaterialUniforms(shader, material, stack);
                        lastMaterial = material;
                    }

                    glDrawArrays(GL_TRIANGLES, vertexData.firstVertex(), vertexData.vertexCount());
                }
            }

        }

        lastVertexData = null;
        lastMaterial = null;
    }

    private void setLightsUniformBuffer(List<LightSource> lightSources, MemoryStack stack) {

        if(lightSources.isEmpty()) {
            return;
        }

        final GLUniformBuffer lightsUniformBuffer = this.lightsUniformBuffer;

        final int lightsCount = min(lightSources.size(), LIGHTS_MAX_COUNT);

        final ByteBuffer buffer = stack.malloc(Light.SIZEOF);

        lightsUniformBuffer.update(LIGHTS_UNIFORM_BUFFER_COUNT_OFFSET, stack.malloc(INT32_SIZEOF).putInt(0, lightsCount));

        for(int i = 0; i < lightsCount; i++) {
            lightSources.get(i).light().get(0, buffer);
            lightsUniformBuffer.update(i * Light.SIZEOF, buffer);
        }
    }

    private void setMaterialUniforms(GLShaderProgram shader, PhongMaterial material, MemoryStack stack) {

        materialUniformBuffer.update(0, material.get(0, stack.malloc(PhongMaterial.SIZEOF)));

        shader.uniformSampler(UNIFORM_AMBIENT_MAP_NAME, material.ambientMap(), 0);
        shader.uniformSampler(UNIFORM_DIFFUSE_MAP_NAME, material.diffuseMap(), 1);
        shader.uniformSampler(UNIFORM_SPECULAR_MAP_NAME, material.specularMap(), 2);
        shader.uniformSampler(UNIFORM_EMISSIVE_MAP_NAME, material.emissiveMap(), 3);
    }

}
