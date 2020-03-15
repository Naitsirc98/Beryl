package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexData;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.resources.Resources;
import naitsirc98.beryl.scenes.components.camera.Camera;
import naitsirc98.beryl.scenes.components.meshes.MeshView;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.util.List;

import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class GLPhongRenderingPath extends RenderingPath {

    private static final Path VERTEX_SHADER_PATH;
    private static final Path FRAGMENT_SHADER_PATH;

    private static final String UNIFORM_MVP_NAME = "u_MVP";

    private static final String UNIFORM_MATERIAL_NAME = "u_Material";
    private static final String UNIFORM_AMBIENT_COLOR_NAME = UNIFORM_MATERIAL_NAME + ".ambientColor";
    private static final String UNIFORM_AMBIENT_MAP_NAME = UNIFORM_MATERIAL_NAME + ".ambientMap";
    private static final String UNIFORM_DIFFUSE_COLOR_NAME = UNIFORM_MATERIAL_NAME + ".diffuseColor";
    private static final String UNIFORM_DIFFUSE_MAP_NAME = UNIFORM_MATERIAL_NAME + ".diffuseMap";
    private static final String UNIFORM_SPECULAR_COLOR_NAME = UNIFORM_MATERIAL_NAME + ".specularColor";
    private static final String UNIFORM_SPECULAR_MAP_NAME = UNIFORM_MATERIAL_NAME + ".specularMap";
    private static final String UNIFORM_EMISSIVE_COLOR_NAME = UNIFORM_MATERIAL_NAME + ".emissiveColor";
    private static final String UNIFORM_EMISSIVE_MAP_NAME = UNIFORM_MATERIAL_NAME + ".emissiveMap";
    private static final String UNIFORM_SHININESS_NAME = UNIFORM_MATERIAL_NAME + ".shininess";

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

        projectionViewMatrix = new Matrix4f();
    }

    @Override
    protected void terminate() {
        shader.free();
    }

    @Override
    public void render(Camera camera, List<MeshView> meshViews) {

        glEnable(GL_DEPTH_TEST);
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        shader.use();

        final int mvpLocation = shader.uniformLocation(UNIFORM_MVP_NAME);

        Matrix4fc projectionView = camera.projectionViewMatrix();
        Matrix4f mvp = projectionViewMatrix;

        try(MemoryStack stack = stackPush()) {

            FloatBuffer mvpData = stack.mallocFloat(16);

            for(MeshView meshView : meshViews) {

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

                glDrawArrays(GL_TRIANGLES, vertexData.firstVertex(), vertexData.vertexCount());
            }
        }

        lastVertexData = null;
        lastMaterial = null;
    }

    private void setMaterialUniforms(GLShaderProgram shader, PhongMaterial material) {

        shader.uniformColor(UNIFORM_AMBIENT_COLOR_NAME, material.ambientColor());
        shader.uniformSampler(UNIFORM_AMBIENT_MAP_NAME, (GLTexture2D) material.ambientMap(), 0);
        shader.uniformColor(UNIFORM_DIFFUSE_COLOR_NAME, material.diffuseColor());
        shader.uniformSampler(UNIFORM_DIFFUSE_MAP_NAME, (GLTexture2D) material.diffuseMap(), 1);
        shader.uniformColor(UNIFORM_SPECULAR_COLOR_NAME, material.specularColor());
        shader.uniformSampler(UNIFORM_SPECULAR_MAP_NAME, (GLTexture2D) material.specularMap(), 2);
        shader.uniformColor(UNIFORM_EMISSIVE_COLOR_NAME, material.emissiveColor());
        shader.uniformSampler(UNIFORM_EMISSIVE_MAP_NAME, (GLTexture2D) material.emissiveMap(), 3);
        shader.uniformFloat(UNIFORM_SHININESS_NAME, material.shininess());
    }

}
