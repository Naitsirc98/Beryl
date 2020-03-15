package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexData;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.resources.Resources;
import naitsirc98.beryl.scenes.Scene;
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
import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public final class GLSimpleRenderingPath extends RenderingPath {

    private static final Path VERTEX_SHADER_PATH;
    private static final Path FRAGMENT_SHADER_PATH;

    private static final String UNIFORM_MVP_NAME = "u_MVP";

    static {

        Path vertexPath = null;
        Path fragmentPath = null;

        try {
            vertexPath = Resources.getPath("shaders/gl/simple/simple.gl.vert");
            fragmentPath = Resources.getPath("shaders/gl/simple/simple.gl.frag");
        } catch (Exception e) {
            Log.fatal("Failed to get shader files for RenderingPath", e);
        }

        VERTEX_SHADER_PATH = vertexPath;
        FRAGMENT_SHADER_PATH = fragmentPath;
    }

    private GLShaderProgram shader;
    private Matrix4f projectionViewMatrix;
    private GLVertexData lastVertexData;

    private GLSimpleRenderingPath() {

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
    public void render(Camera camera, Scene scene) {

        glEnable(GL_DEPTH_TEST);
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        shader.use();

        final int mvpLocation = shader.uniformLocation(UNIFORM_MVP_NAME);

        Matrix4fc projectionView = camera.projectionViewMatrix();
        Matrix4f mvp = projectionViewMatrix;

        try(MemoryStack stack = stackPush()) {

            FloatBuffer mvpData = stack.mallocFloat(16);

            for(MeshView meshView : scene.meshViews()) {

                GLVertexData vertexData = meshView.mesh().vertexData();

                if(lastVertexData != vertexData) {
                    vertexData.bind();
                    lastVertexData = vertexData;
                }

                glUniformMatrix4fv(mvpLocation, false, projectionView.mul(meshView.modelMatrix(), mvp).get(mvpData));

                glDrawArrays(GL_TRIANGLES, vertexData.firstVertex(), vertexData.vertexCount());
            }
        }

        lastVertexData = null;
    }
}
