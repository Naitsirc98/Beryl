package naitsirc98.beryl.graphics.opengl.shaders;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.Destructor;
import naitsirc98.beryl.util.IntHandle;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;

import java.util.*;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.stackPush;

@Destructor
public final class GLShaderProgram implements IntHandle, NativeResource {

    private final int handle;
    private Set<GLShader> shaders;
    private Map<String, Integer> uniformLocations;

    public GLShaderProgram() {
        handle = glCreateProgram();
        shaders = new HashSet<>();
        uniformLocations = new WeakHashMap<>();
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

    public int uniformLocation(String name) {
        return uniformLocations.computeIfAbsent(name, k -> glGetUniformLocation(handle, name));
    }

    public GLShaderProgram uniformMatrix4f(String name, boolean transpose, Matrix4fc value) {
        try(MemoryStack stack = stackPush()) {
            glUniformMatrix4fv(uniformLocation(name), transpose, value.get(stack.mallocFloat(16)));
        }
        return this;
    }

    @Override
    public void free() {
        glDeleteProgram(handle);
        shaders = null;
        uniformLocations = null;
    }
}
