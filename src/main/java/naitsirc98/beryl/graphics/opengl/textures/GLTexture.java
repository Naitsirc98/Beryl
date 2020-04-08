package naitsirc98.beryl.graphics.opengl.textures;

import naitsirc98.beryl.graphics.opengl.GLObject;
import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.resources.ManagedResource;

import static org.lwjgl.opengl.ARBBindlessTexture.*;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL45.glCreateTextures;
import static org.lwjgl.opengl.GL45C.glBindTextureUnit;

public abstract class GLTexture extends ManagedResource implements GLObject, Texture {

    protected int handle;
    private GLSampler sampler;
    private long residentHandle;

    public GLTexture(int target) {
        this.handle = glCreateTextures(target);
        sampler = new GLSampler();
    }

    @Override
    public int handle() {
        return handle;
    }

    public long handleARB() {
        return glGetTextureSamplerHandleARB(handle, sampler.handle());
    }

    public long makeResident() {
        if(residentHandle == NULL) {
            residentHandle = handleARB();
            glMakeTextureHandleResidentARB(residentHandle);
        }
        return residentHandle;
    }

    public void makeNonResident() {
        if(residentHandle != NULL) {
            glMakeTextureHandleNonResidentARB(handleARB());
        }
    }

    @Override
    public GLSampler sampler() {
        return sampler;
    }

    public void bind(int unit) {
        glBindTextureUnit(unit, handle());
        sampler.bind(unit);
    }

    @Override
    protected void free() {
        makeNonResident();
        glDeleteTextures(handle);
        sampler.release();

        handle = NULL;
        sampler = null;
    }
}
