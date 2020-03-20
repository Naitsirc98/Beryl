package naitsirc98.beryl.graphics.opengl.shaders;

import naitsirc98.beryl.graphics.ShaderStage;
import naitsirc98.beryl.graphics.opengl.GLObject;
import naitsirc98.beryl.graphics.shaders.GLSLPreprocessor;
import naitsirc98.beryl.logging.Log;

import java.nio.file.Path;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.opengl.GL46C.*;

public final class GLShader implements GLObject {

    private final int handle;
    private final ShaderStage stage;
    private Path path;

    public GLShader(ShaderStage stage) {
        this.handle = glCreateShader(stage.handle());
        this.stage = stage;
    }

    @Override
    public int handle() {
        return handle;
    }

    public ShaderStage stage() {
        return stage;
    }

    public GLShader source(Path path) {
        this.path = requireNonNull(path);
        glShaderSource(handle, new GLSLPreprocessor(path, stage).process());
        return this;
    }

    public GLShader compile() {
        glCompileShader(handle);
        checkCompilationStatus();
        return this;
    }

    private void checkCompilationStatus() {
        if(glGetShaderi(handle, GL_COMPILE_STATUS) != GL_TRUE) {
            Log.fatal("Failed to compile OpenGL shader " + this + ":\n"
                + glGetShaderInfoLog(handle));
        }
    }

    @Override
    public void release() {
        glDeleteShader(handle);
        path = null;
    }

    @Override
    public String toString() {
        return "GLShader{" +
                "handle=" + handle +
                ", stage=" + stage +
                ", path=" + path +
                '}';
    }
}
