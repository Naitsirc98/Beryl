package naitsirc98.beryl.graphics.opengl.shaders;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.Destructor;
import naitsirc98.beryl.util.IntHandle;
import org.lwjgl.system.NativeResource;

import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.opengl.GL20.*;

@Destructor
public final class GLShaderProgram implements IntHandle, NativeResource {

    private final int handle;
    private Set<GLShader> shaders;

    public GLShaderProgram() {
        handle = glCreateProgram();
        shaders = new HashSet<>();
    }

    @Override
    public int handle() {
        return handle;
    }

    public GLShaderProgram attach(GLShader shader) {
        if(shaders.add(shader)) {
            glAttachShader(handle, shader.handle());
        }
        return this;
    }

    public GLShaderProgram link() {
        glLinkProgram(handle);
        checkLinkStatus();
        deleteShaders();
        return this;
    }

    public void use() {
        glUseProgram(handle);
    }

    public void deleteShaders() {
        for(GLShader shader : shaders) {
            glDetachShader(handle, shader.handle());
            shader.free();
        }
        shaders.clear();
    }

    private void checkLinkStatus() {
        if(glGetProgrami(handle, GL_LINK_STATUS) != GL_TRUE) {
            Log.fatal("Failed to compile OpenGL shader program(" + handle + "):\n"
                    + glGetProgramInfoLog(handle) + shaderSources());
        }
    }

    private String shaderSources() {

        StringBuilder builder = new StringBuilder();

        for(GLShader shader : shaders) {
            builder.append('\n').append(shader.stage()).append(" shader source:\n");
            builder.append(shader.source());
        }

        return builder.toString();
    }

    @Override
    public void free() {
        glDeleteProgram(handle);
        shaders = null;
    }
}
