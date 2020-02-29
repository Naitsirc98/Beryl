package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.vertex.GLVertexData;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.resources.Resources;
import naitsirc98.beryl.scenes.components.camera.Camera;
import naitsirc98.beryl.scenes.components.meshes.MeshView;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.nio.file.Path;
import java.util.List;

import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static org.lwjgl.opengl.GL11.*;

public final class GLSimpleRenderingPath extends RenderingPath {

    private static final Path VERTEX_SHADER_PATH;
    private static final Path FRAGMENT_SHADER_PATH;

    private static final String UNIFORM_NAME_MODEL_VIEW_PROJECTION = "u_ProjectionViewModel";

    static {

        Path vertexPath = null;
        Path fragmentPath = null;

        try {
            vertexPath = Resources.getPath("shaders/simple/simple.vert");
            fragmentPath = Resources.getPath("shaders/simple/simple.frag");
        } catch (Exception e) {
            Log.fatal("Failed to get shader files for RenderingPath", e);
        }

        VERTEX_SHADER_PATH = vertexPath;
        FRAGMENT_SHADER_PATH = fragmentPath;
    }

    private GLShaderProgram shader;
    private Matrix4f projectionViewModelMatrix;

    private GLSimpleRenderingPath() {

    }

    @Override
    protected void init() {
        shader = new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(VERTEX_SHADER_PATH).compile())
                .attach(new GLShader(FRAGMENT_STAGE).source(FRAGMENT_SHADER_PATH).compile())
                .link();
        projectionViewModelMatrix = new Matrix4f();
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

        Matrix4fc projectionView = camera.projectionViewMatrix();
        Matrix4f mvp = projectionViewModelMatrix;

        for(MeshView meshView : meshViews) {

            GLVertexData vertexData = meshView.mesh().vertexData();

            Matrix4fc modelMatrix = meshView.transform().modelMatrix();

            shader.uniformMatrix4f("u_MVP", false, projectionView.mul(modelMatrix, mvp));

            vertexData.bind();

            glDrawArrays(GL_TRIANGLES, 0, 36);
        }
    }
}
