package naitsirc98.beryl.graphics.opengl.buffers;

import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;

import java.util.HashMap;
import java.util.Map;

import static naitsirc98.beryl.util.Asserts.assertThat;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL44.GL_MAP_COHERENT_BIT;
import static org.lwjgl.opengl.GL45.GL_DYNAMIC_STORAGE_BIT;
import static org.lwjgl.opengl.GL45.GL_MAP_PERSISTENT_BIT;

public class GLUniformBuffer extends GLBuffer {

    private final Map<GLShaderProgram, Integer> bindings;

    public GLUniformBuffer() {
        bindings = new HashMap<>();
    }

    public GLUniformBuffer set(String nameOfBlock, GLShaderProgram shader, int binding) {
        final int blockIndex = glGetUniformBlockIndex(shader.handle(), nameOfBlock);
        glUniformBlockBinding(shader.handle(), assertThat(blockIndex, blockIndex >= 0), binding);
        bindings.put(shader, binding);
        return this;
    }

    public int binding(GLShaderProgram shader) {
        return bindings.get(shader);
    }

    public void bind(GLShaderProgram shader) {
        glBindBufferBase(GL_UNIFORM_BUFFER, bindings.get(shader), handle());
    }

    public void bind(GLShaderProgram shader, long offset, long size) {
        glBindBufferRange(GL_UNIFORM_BUFFER, bindings.get(shader), handle(), offset, size);
    }

    @Override
    public Type type() {
        return Type.UNIFORM_BUFFER;
    }
}
