package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.scenes.components.camera.Camera;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.ClassLoader.getSystemClassLoader;
import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static org.lwjgl.opengl.GL45.*;

public final class GLSimpleRenderingPath extends RenderingPath {

    private static final Path VERTEX_SHADER_PATH;
    private static final Path FRAGMENT_SHADER_PATH;

    private static final String UNIFORM_NAME_MODEL_VIEW_PROJECTION = "u_ProjectionViewModel";

    static {

        Path vertexPath = null;
        Path fragmentPath = null;

        try {
            vertexPath = Paths.get(new URI(getSystemClassLoader().getResource("shaders/simple.vert").getFile()));
            fragmentPath = Paths.get(new URI(getSystemClassLoader().getResource("shaders/simple.vert").getFile()));
        } catch (URISyntaxException e) {
            Log.fatal("Failed to get shader files for RenderingPath", e);
        }

        VERTEX_SHADER_PATH = vertexPath;
        FRAGMENT_SHADER_PATH = fragmentPath;
    }

    private GLShaderProgram shader;

    @Override
    protected void init() {
        shader = new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(VERTEX_SHADER_PATH).compile())
                .attach(new GLShader(FRAGMENT_STAGE).source(FRAGMENT_SHADER_PATH).compile())
                .link();
    }

    @Override
    protected void terminate() {
        shader.free();
    }

    @Override
    public void render(Camera camera) {

        shader.use();

        // TODO

        glDrawArrays(GL_TRIANGLES, 0, 36);
    }
}
