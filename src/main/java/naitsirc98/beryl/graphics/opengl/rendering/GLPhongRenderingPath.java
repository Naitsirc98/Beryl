package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.graphics.opengl.buffers.GLUniformBuffer;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexData;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.lights.IPointLight;
import naitsirc98.beryl.lights.Light;
import naitsirc98.beryl.lights.SpotLight;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.resources.Resources;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.camera.Camera;
import naitsirc98.beryl.scenes.components.lights.LightSource;
import naitsirc98.beryl.scenes.components.meshes.MeshView;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.util.List;

import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.util.types.ByteSizeUtils.sizeof;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

public class GLPhongRenderingPath extends RenderingPath {

    private static final Path VERTEX_SHADER_PATH;
    private static final Path FRAGMENT_SHADER_PATH;

    private static final int MATRICES_UNIFORM_BUFFER_SIZE = (16 + 16) * FLOAT32_SIZEOF;
    private static final String MATRICES_UNIFORM_BUFFER_NAME = "MatricesUniformBuffer";

    private static final int MATERIAL_UNIFORM_BUFFER_SIZE = PhongMaterial.SIZEOF;
    private static final String MATERIAL_UNIFORM_BUFFER_NAME = "MaterialUniformBuffer";

    private static final int LIGHTS_UNIFORM_BUFFER_SIZE = (SpotLight.SIZEOF + FLOAT32_SIZEOF) * 64 + FLOAT32_SIZEOF;
    private static final String LIGHTS_UNIFORM_BUFFER_NAME = "LightsUniformBuffer";

    private static final String UNIFORM_MVP_NAME = "u_MVP";

    private static final String UNIFORM_AMBIENT_MAP_NAME = "u_AmbientMap";
    private static final String UNIFORM_DIFFUSE_MAP_NAME = "u_DiffuseMap";
    private static final String UNIFORM_SPECULAR_MAP_NAME =  "u_SpecularMap";
    private static final String UNIFORM_EMISSIVE_MAP_NAME =  "u_EmissiveMap";

    static {

        Path vertexPath = null;
        Path fragmentPath = null;

        try {
            vertexPath = Resources.getPath("shaders/gl/phong/phong.gl.vert");
            fragmentPath = Resources.getPath("shaders/gl/phong/phong.gl.frag");
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

    private FloatBuffer matricesUniformBufferData;
    private ByteBuffer lightsUniformBufferData;
    private FloatBuffer mvpData;

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

        matricesUniformBufferData = memAllocFloat(MATRICES_UNIFORM_BUFFER_SIZE / FLOAT32_SIZEOF);

        materialUniformBuffer = new GLUniformBuffer(MATERIAL_UNIFORM_BUFFER_NAME, shader, 1);
        materialUniformBuffer.allocate(MATERIAL_UNIFORM_BUFFER_SIZE);

        lightsUniformBuffer = new GLUniformBuffer(LIGHTS_UNIFORM_BUFFER_NAME, shader, 2);
        lightsUniformBuffer.allocate(LIGHTS_UNIFORM_BUFFER_SIZE);

        lightsUniformBufferData = memAlloc(LIGHTS_UNIFORM_BUFFER_SIZE);

        mvpData = memAllocFloat(16);

        projectionViewMatrix = new Matrix4f();
    }

    @Override
    protected void terminate() {

        shader.free();

        matricesUniformBuffer.free();
        materialUniformBuffer.free();
        lightsUniformBuffer.free();

        memFree(matricesUniformBufferData);
        memFree(lightsUniformBufferData);
        memFree(mvpData);
    }

    @Override
    public void render(Camera camera, Scene scene) {

        glEnable(GL_DEPTH_TEST);
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        final GLShaderProgram shader = this.shader;
        final GLUniformBuffer matricesUniformBuffer = this.matricesUniformBuffer;

        shader.use();

        shader.uniformVector3f("u_CameraPosition", camera.transform().position());

        setLightsUniformBuffer(scene.lightSources());

        matricesUniformBuffer.bind();

        materialUniformBuffer.bind();

        final int mvpLocation = shader.uniformLocation(UNIFORM_MVP_NAME);

        final Matrix4fc projectionView = camera.projectionViewMatrix();
        final Matrix4f mvp = projectionViewMatrix;
        final FloatBuffer mvpData = this.mvpData;
        final FloatBuffer matricesUniformBufferData = this.matricesUniformBufferData;

        for(MeshView meshView : scene.meshViews()) {

            final GLVertexData vertexData = meshView.mesh().vertexData();
            final PhongMaterial material = meshView.mesh().material();

            if(lastVertexData != vertexData) {
                vertexData.bind();
                lastVertexData = vertexData;
            }

            if(lastMaterial != material) {
                setMaterialUniforms(shader, material);
                lastMaterial = material;
            }

            shader.uniformMatrix4f(mvpLocation, false, projectionView.mul(meshView.modelMatrix(), mvp).get(mvpData));

            meshView.modelMatrix().get(0, matricesUniformBufferData);
            meshView.normalMatrix().get(16, matricesUniformBufferData);

            matricesUniformBuffer.update(0, matricesUniformBufferData.rewind());

            glDrawArrays(GL_TRIANGLES, vertexData.firstVertex(), vertexData.vertexCount());
        }

        lastVertexData = null;
        lastMaterial = null;
    }

    private void setLightsUniformBuffer(List<LightSource> lightSources) {

        if(lightSources.isEmpty()) {
            return;
        }

        lightsUniformBuffer.bind();

        final ByteBuffer lightsUniformBufferData = this.lightsUniformBufferData;

        lightsUniformBufferData.putFloat(lightSources.size());

        final int minLightSize = DirectionalLight.SIZEOF;

        for(LightSource lightSource : lightSources) {

            final Light<?> light = lightSource.light();

            if(sizeof(light) <= lightsUniformBufferData.remaining()) {
                lightsUniformBufferData.putFloat(light.type());
                light.get(lightsUniformBufferData);
            }

            if(lightsUniformBufferData.remaining() < minLightSize) {
                break;
            }
        }

        lightsUniformBuffer.update(0, lightsUniformBufferData.flip());

        lightsUniformBufferData.limit(lightsUniformBufferData.capacity());
    }

    private void setMaterialUniforms(GLShaderProgram shader, PhongMaterial material) {

        try(MemoryStack stack = stackPush()) {
            final FloatBuffer uniformBufferData = material.get(stack.mallocFloat(PhongMaterial.FLOAT_BUFFER_MIN_SIZE));
            materialUniformBuffer.update(0, uniformBufferData.rewind());
        }

        shader.uniformSampler(UNIFORM_AMBIENT_MAP_NAME, material.ambientMap(), 0);
        shader.uniformSampler(UNIFORM_DIFFUSE_MAP_NAME, material.diffuseMap(), 1);
        shader.uniformSampler(UNIFORM_SPECULAR_MAP_NAME, material.specularMap(), 2);
        shader.uniformSampler(UNIFORM_EMISSIVE_MAP_NAME, material.emissiveMap(), 3);
    }

}
