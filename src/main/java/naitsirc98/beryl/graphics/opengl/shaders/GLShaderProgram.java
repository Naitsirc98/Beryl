package naitsirc98.beryl.graphics.opengl.shaders;

import naitsirc98.beryl.graphics.opengl.GLObject;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.Color;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public final class GLShaderProgram implements GLObject {

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
            shader.release();
        }
        shaders.clear();
    }

    private void checkLinkStatus() {
        if(glGetProgrami(handle, GL_LINK_STATUS) != GL_TRUE) {
            Log.fatal("Failed to compile OpenGL shader program(" + handle + "):\n"
                    + glGetProgramInfoLog(handle));
        }
    }

    public int uniformLocation(String name) {
        return uniformLocations.computeIfAbsent(name, k -> glGetUniformLocation(handle, name));
    }

    public void uniformMatrix4f(String name, boolean transpose, FloatBuffer value) {
        glUniformMatrix4fv(uniformLocation(name), transpose, value);
    }

    public void uniformMatrix4f(int location, boolean transpose, FloatBuffer value) {
        glUniformMatrix4fv(location, transpose, value);
    }

    public void uniformVector4f(String name, Vector3fc vector) {
        uniformVector4f(uniformLocation(name), vector.x(), vector.y(), vector.z(), 1.0f);
    }

    public void uniformVector4f(String name, Vector4fc vector) {
        uniformVector4f(uniformLocation(name), vector);
    }

    public void uniformVector4f(int location, Vector4fc data) {
        glUniform4f(location, data.x(), data.y(), data.z(), data.w());
    }

    public void uniformVector4f(int location, float x, float y, float z, float w) {
        glUniform4f(location, x, y, z, w);
    }

    public void uniformVector3f(String name, Vector3fc vector) {
        uniformVector3f(uniformLocation(name), vector);
    }

    public void uniformVector3f(int location, Vector3fc data) {
        glUniform3f(location, data.x(), data.y(), data.z());
    }

    public void uniformColor(String name, Color color) {
        uniformColor(uniformLocation(name), color);
    }

    public void uniformColor(int location, Color color) {
        glUniform4f(location, color.red(), color.green(), color.blue(), color.alpha());
    }

    public void uniformSampler(String name, GLTexture texture, int unit) {
        uniformSampler(uniformLocation(name), texture, unit);
    }

    public void uniformSampler(int location, GLTexture texture, int unit) {
        glUniform1i(location, unit);
        texture.bind(unit);
    }

    public void uniformFloat(String name, float value) {
        uniformFloat(uniformLocation(name), value);
    }

    public void uniformFloat(int location, float value) {
        glUniform1f(location, value);
    }

    @Override
    public void release() {
        glDeleteProgram(handle);
        shaders = null;
        uniformLocations = null;
    }
}
