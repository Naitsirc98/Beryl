package naitsirc98.beryl.graphics.opengl.textures;

import naitsirc98.beryl.graphics.opengl.GLObject;
import naitsirc98.beryl.graphics.textures.SamplerInfo;
import naitsirc98.beryl.graphics.textures.Texture;

import static naitsirc98.beryl.graphics.opengl.GLUtils.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.opengl.GL45C.glBindTextureUnit;

public abstract class GLTexture implements GLObject, Texture {

    protected int handle;

    public GLTexture(int target) {
        this.handle = glCreateTextures(target);
    }

    @Override
    public int handle() {
        return handle;
    }

    public void bind(int unit) {
        glBindTextureUnit(unit, handle());
    }

    @Override
    public WrapMode wrapModeS() {
        return glToWrapMode(glGetTextureParameteri(handle, GL_TEXTURE_WRAP_S));
    }

    @Override
    public WrapMode wrapModeT() {
        return glToWrapMode(glGetTextureParameteri(handle, GL_TEXTURE_WRAP_T));
    }

    @Override
    public WrapMode wrapModeR() {
        return glToWrapMode(glGetTextureParameteri(handle, GL_TEXTURE_WRAP_R));
    }

    @Override
    public Filter minFilter() {
        return glToFilter(glGetTextureParameteri(handle, GL_TEXTURE_MIN_FILTER));
    }

    @Override
    public Filter magFilter() {
        return glToFilter(glGetTextureParameteri(handle, GL_TEXTURE_MAG_FILTER));
    }

    @Override
    public void samplerInfo(SamplerInfo samplerInfo) {

        glTextureParameteri(handle, GL_TEXTURE_WRAP_S, toGL(samplerInfo.wrapModeS()));
        glTextureParameteri(handle, GL_TEXTURE_WRAP_T, toGL(samplerInfo.wrapModeT()));
        glTextureParameteri(handle, GL_TEXTURE_WRAP_R, toGL(samplerInfo.wrapModeR()));

        glTextureParameteri(handle, GL_TEXTURE_MIN_FILTER, toGL(samplerInfo.minFilter()));
        glTextureParameteri(handle, GL_TEXTURE_MAG_FILTER, toGL(samplerInfo.magFilter()));
    }

    @Override
    public void free() {
        glDeleteTextures(handle);
        handle = NULL;
    }
}
