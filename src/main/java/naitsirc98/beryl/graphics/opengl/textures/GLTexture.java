package naitsirc98.beryl.graphics.opengl.textures;

import naitsirc98.beryl.graphics.opengl.GLObject;
import naitsirc98.beryl.graphics.textures.Texture;

import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL45.glCreateTextures;
import static org.lwjgl.opengl.GL45C.glBindTextureUnit;

public abstract class GLTexture implements GLObject, Texture {

    protected int handle;
    private GLSampler sampler;

    public GLTexture(int target) {
        this.handle = glCreateTextures(target);
        sampler = new GLSampler();
    }

    @Override
    public int handle() {
        return handle;
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
    public void free() {
        glDeleteTextures(handle);
        sampler.free();

        handle = NULL;
        sampler = null;
    }
}
