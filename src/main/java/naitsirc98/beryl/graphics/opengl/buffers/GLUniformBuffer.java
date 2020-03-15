package naitsirc98.beryl.graphics.opengl.buffers;

import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;

import static naitsirc98.beryl.util.Asserts.assertThat;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL31.*;

public class GLUniformBuffer extends GLBuffer {

    private final int binding;

    public GLUniformBuffer(String name, GLShaderProgram shader, int binding) {
        this.binding = binding;
        final int blockIndex = glGetUniformBlockIndex(shader.handle(), name);
        glUniformBlockBinding(shader.handle(), assertThat(blockIndex, blockIndex >= 0), binding);
    }

    public int binding() {
        return binding;
    }

    public void bind() {
        glBindBufferBase(GL_UNIFORM_BUFFER, binding, handle());
    }

    public void bind(long offset, long size) {
        glBindBufferRange(GL_UNIFORM_BUFFER, binding, handle(), offset, size);
    }

    @Override
    public Type type() {
        return Type.UNIFORM_BUFFER;
    }
}
