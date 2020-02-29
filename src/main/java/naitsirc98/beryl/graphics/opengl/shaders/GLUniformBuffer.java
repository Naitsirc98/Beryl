package naitsirc98.beryl.graphics.opengl.shaders;

import naitsirc98.beryl.graphics.opengl.GLBuffer;

import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL31.*;

public class GLUniformBuffer extends GLBuffer {

    private final int binding;

    public GLUniformBuffer(String name, GLShaderProgram shader, int binding) {
        this.binding = binding;
        glUniformBlockBinding(shader.handle(), glGetUniformBlockIndex(shader.handle(), name), binding);
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
}
