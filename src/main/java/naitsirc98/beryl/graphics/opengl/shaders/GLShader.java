package naitsirc98.beryl.graphics.opengl.shaders;

import naitsirc98.beryl.graphics.ShaderStage;
import naitsirc98.beryl.graphics.opengl.GLObject;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.Destructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL45.*;

@Destructor
public final class GLShader implements GLObject {

    private final int handle;
    private final ShaderStage stage;
    private String source;

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

    public String source() {
        return source;
    }

    public GLShader source(Path path) {
        try {
            return source(new String(Files.readAllBytes(path)));
        } catch (IOException e) {
            Log.fatal("Failed to read shader source file: " + path);
        }
        return this;
    }

    public GLShader source(String source) {
        glShaderSource(handle, source);
        this.source = source;
        return this;
    }

    public GLShader compile() {
        glCompileShader(handle);
        checkCompilationStatus();
        return this;
    }

    private void checkCompilationStatus() {
        if(glGetShaderi(handle, GL_COMPILE_STATUS) != GL_TRUE) {
            Log.fatal("Failed to compile " + stage + " OpenGL shader(" + handle + "):\n"
                + glGetShaderInfoLog(handle) + "\nSource:\n" + source);
        }
    }

    @Override
    public void free() {
        glDeleteShader(handle);
        source = null;
    }
}
