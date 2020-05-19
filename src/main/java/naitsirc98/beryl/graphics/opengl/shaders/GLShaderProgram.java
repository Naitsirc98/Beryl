package naitsirc98.beryl.graphics.opengl.shaders;

import naitsirc98.beryl.graphics.opengl.GLObject;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.Color;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.*;
import java.util.Map.Entry;

import static naitsirc98.beryl.core.Beryl.DEBUG;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public final class GLShaderProgram implements GLObject {

    private final int handle;
    private final String name;
    private Set<GLShader> shaders;
    private Map<String, Integer> uniformLocations;
    private Map<Integer, GLTexture> boundTextures;

    public GLShaderProgram(String name) {
        handle = glCreateProgram();
        shaders = new HashSet<>();
        uniformLocations = new WeakHashMap<>();
        boundTextures = new HashMap<>();
        this.name = name;
    }

    public GLShaderProgram() {
        this("");
    }

    @Override
    public int handle() {
        return handle;
    }

    public String name() {
        return name;
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
        unbindTextures();
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

            if(location == -1) {
                Log.warning("Uniform " + name + " does not exists or is not used in shader " + this);
            }

            uniformLocations.put(name, location);
        }

        return location;
    }

    public void uniformMatrix4f(String name, boolean transpose, Matrix4fc matrix) {
        try(MemoryStack stack = stackPush()) {
            uniformMatrix4f(name, transpose, matrix.get(stack.mallocFloat(16)));
        }
    }

    public void uniformMatrix4f(String name, boolean transpose, FloatBuffer value) {
        uniformMatrix4f(uniformLocation(name), transpose, value);
    }

    public void uniformMatrix4f(int location, boolean transpose, FloatBuffer value) {
        checkUniformLocation("mat4", location);
        glUniformMatrix4fv(location, transpose, value);
    }

    public void uniformVector4f(String name, Vector3fc vector) {
        uniformVector4f(uniformLocation(name), vector.x(), vector.y(), vector.z(), 1.0f);
    }

    public void uniformVector4f(String name, Vector4fc vector) {
        uniformVector4f(uniformLocation(name), vector);
    }

    public void uniformVector4f(int location, Vector4fc data) {
        uniformVector4f(location, data.x(), data.y(), data.z(), data.w());
    }

    public void uniformVector4f(String name, float x, float y, float z, float w) {
        uniformVector4f(uniformLocation(name), x, y, z, w);
    }

    public void uniformVector4f(int location, float x, float y, float z, float w) {
        checkUniformLocation("vec4", location);
        glUniform4f(location, x, y, z, w);
    }

    public void uniformVector3f(String name, Vector3fc vector) {
        uniformVector3f(uniformLocation(name), vector);
    }

    public void uniformVector3f(int location, Vector3fc data) {
        checkUniformLocation("vec3", location);
        glUniform3f(location, data.x(), data.y(), data.z());
    }

    public void uniformColorRGBA(String name, Color color) {
        uniformColorRGBA(uniformLocation(name), color);
    }

    public void uniformColorRGBA(int location, Color color) {
        checkUniformLocation("color (vec4)", location);
        glUniform4f(location, color.red(), color.green(), color.blue(), color.alpha());
    }

    public void uniformColorRGB(String name, Color color) {
        uniformColorRGB(uniformLocation(name), color);
    }

    public void uniformColorRGB(int location, Color color) {
        checkUniformLocation("color (vec3)", location);
        glUniform3f(location, color.red(), color.green(), color.blue());
    }

    public void uniformSampler(String name, GLTexture texture, int unit) {
        uniformSampler(uniformLocation(name), texture, unit);
    }

    public void uniformSampler(int location, GLTexture texture, int unit) {
        checkUniformLocation("sampler", location);
        glUniform1i(location, unit);
        texture.bind(unit);
        boundTextures.put(unit, texture);
    }

    public void uniformFloat(String name, float value) {
        uniformFloat(uniformLocation(name), value);
    }

    public void uniformFloat(int location, float value) {
        checkUniformLocation("float", location);
        glUniform1f(location, value);
    }

    public void uniformInt(String name, int value) {
        uniformInt(uniformLocation(name), value);
    }

    public void uniformInt(int location, int value) {
        checkUniformLocation("int", location);
        glUniform1i(location, value);
    }

    public void uniformBool(String name, boolean value) {
        uniformInt(name, value ? 1 : 0);
    }

    public void unbindTextures() {

        if(boundTextures.isEmpty()) {
            return;
        }

        Iterator<Entry<Integer, GLTexture>> iterator = boundTextures.entrySet().iterator();

        while(iterator.hasNext()) {

            Entry<Integer, GLTexture> textureUnit = iterator.next();

            final GLTexture texture = textureUnit.getValue();
            final int unit = textureUnit.getKey();

            texture.unbind(unit);

            iterator.remove();
        }

    }

    @Override
    public void release() {
        glDeleteProgram(handle);
        shaders = null;
        uniformLocations = null;
    }

    private void checkUniformLocation(String uniform, int location) {
        if(DEBUG) {
            if(location < 0) {
                // Log.warning("Location of uniform " + uniform + " is invalid: " + location);
            }
        }
    }

    @Override
    public String toString() {
        return "GLShaderProgram{" +
                "handle=" + handle +
                ", name='" + name + '\'' +
                '}';
    }
}
