package naitsirc98.beryl.graphics.opengl.swapchain;

import naitsirc98.beryl.graphics.opengl.GLObject;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL45.*;

public class GLFramebuffer implements GLObject {

    public static void bindDefault() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private final int handle;
    private final Map<Integer, GLObject> attachments;
    private boolean freeAttachmentsOnRelease;

    public GLFramebuffer() {
        handle = glCreateFramebuffers();
        attachments = new HashMap<>();
    }

    public boolean freeAttachmentsOnRelease() {
        return freeAttachmentsOnRelease;
    }

    public void freeAttachmentsOnRelease(boolean freeAttachmentsOnRelease) {
        this.freeAttachmentsOnRelease = freeAttachmentsOnRelease;
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, handle);
    }

    public void ensureComplete() {
        final int status = glCheckNamedFramebufferStatus(handle, GL_FRAMEBUFFER);
        if(status != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Framebuffer is not complete: " + status);
        }
    }

    public void attach(int attachment, GLRenderbuffer renderbuffer) {
        glNamedFramebufferRenderbuffer(handle, attachment, GL_RENDERBUFFER, renderbuffer.handle());
        attachments.put(attachment, renderbuffer);
    }

    public void attach(int attachment, GLTexture texture, int level) {
        glNamedFramebufferTexture(handle, attachment, texture.handle(), level);
        attachments.put(attachment, texture);
    }

    public void drawBuffer(int drawBuffer) {
        glNamedFramebufferDrawBuffer(handle, drawBuffer);
    }

    public void drawBuffers(IntBuffer drawBuffers) {
        glNamedFramebufferDrawBuffers(handle, drawBuffers);
    }

    public void readBuffer(int readBuffer) {
        glNamedFramebufferReadBuffer(handle, readBuffer);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(int attachment) {
        return (T) attachments.get(attachment);
    }

    @Override
    public int handle() {
        return handle;
    }

    @Override
    public void release() {
        if(freeAttachmentsOnRelease) {
            attachments.values().forEach(GLObject::release);
        }
        attachments.clear();
        glDeleteFramebuffers(handle);
    }
}