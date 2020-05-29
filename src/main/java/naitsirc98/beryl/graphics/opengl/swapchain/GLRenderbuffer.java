package naitsirc98.beryl.graphics.opengl.swapchain;

import naitsirc98.beryl.graphics.opengl.GLContext;
import naitsirc98.beryl.graphics.opengl.GLObject;

import static org.lwjgl.opengl.GL30C.glDeleteRenderbuffers;
import static org.lwjgl.opengl.GL45C.*;

public class GLRenderbuffer extends GLObject {

    public GLRenderbuffer(GLContext context) {
        super(context, glCreateRenderbuffers());
    }

    public void bind() {
        glBindRenderbuffer(GL_RENDERBUFFER, handle());
    }

    public void unbind() {
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
    }

    public void storage(int width, int height, int internalFormat) {
        glNamedRenderbufferStorage(handle(), internalFormat, width, height);
    }

    public void storageMultisample(int width, int height, int internalFormat, int samples) {
        glNamedRenderbufferStorageMultisample(handle(), samples, internalFormat, width, height);
    }

    public int samples() {
        return glGetNamedRenderbufferParameteri(handle(), GL_SAMPLES);
    }

    public boolean multisampled() {
        return samples() > 1;
    }

    @Override
    public void free() {
        glDeleteRenderbuffers(handle());
        setHandle(NULL);
    }
}
