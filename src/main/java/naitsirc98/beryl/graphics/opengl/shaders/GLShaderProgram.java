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

    public void bind() {
        glUseProgram(handle);
    }

    public void unbind() {
        glUseProgram(0);
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

        int location = uniformLocations.getOrDefault(name, Integer.MIN_VALUE);

        if(location == Integer.MIN_VALUE) {
            location = glGetUniformLocation(handle, name);
            uniformLocations.put(name, location);
        }

        return location;
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

    public void uniformVector4f(String name, float x, float y, float z, float w) {
        glUniform4f(uniformLocation(name), x, y, z, w);
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

    public void uniformColorRGBA(String name, Color color) {
        uniformColorRGBA(uniformLocation(name), color);
    }

    public void uniformColorRGBA(int location, Color color) {
        glUniform4f(location, color.red(), color.green(), color.blue(), color.alpha());
    }

    public void uniformColorRGB(String name, Color color) {
        uniformColorRGB(uniformLocation(name), color);
    }

    public void uniformColorRGB(int location, Color color) {
        glUniform3f(location, color.red(), color.green(), color.blue());
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

    public void uniformInt(String name, int value) {
        glUniform1i(uniformLocation(name), value);
    }

    public void uniformBool(String name, boolean value) {
        glUniform1f(uniformLocation(name), value ? 1 : 0);
    }

    @Override
    public void release() {
        glDeleteProgram(handle);
        shaders = null;
        uniformLocations = null;
    }
}
