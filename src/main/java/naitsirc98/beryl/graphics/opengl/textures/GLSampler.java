package naitsirc98.beryl.graphics.opengl.textures;

import naitsirc98.beryl.graphics.opengl.GLObject;
import naitsirc98.beryl.graphics.textures.Sampler;

import static naitsirc98.beryl.graphics.opengl.GLUtils.toGL;
import static org.lwjgl.opengl.GL33.*;

public final class GLSampler implements GLObject, Sampler {

    private int handle;

    public GLSampler() {
        handle = glGenSamplers();
    }

    @Override
    public int handle() {
        return 0;
    }

    @Override
    public void free() {
        glDeleteSamplers(handle);
        handle = NULL;
    }

    @Override
    public void wrapModeS(WrapMode wrapMode) {
        glSamplerParameteri(handle, GL_TEXTURE_WRAP_S, toGL(wrapMode));
    }

    @Override
    public void wrapModeT(WrapMode wrapMode) {
        glSamplerParameteri(handle, GL_TEXTURE_WRAP_T, toGL(wrapMode));
    }

    @Override
    public void wrapModeR(WrapMode wrapMode) {
        glSamplerParameteri(handle, GL_TEXTURE_WRAP_R, toGL(wrapMode));
    }

    @Override
    public void minFilter(Filter filter) {
        glSamplerParameteri(handle, GL_TEXTURE_MIN_FILTER, toGL(filter));
    }

    @Override
    public void magFilter(Filter filter) {
        glSamplerParameteri(handle, GL_TEXTURE_MAG_FILTER, toGL(filter));
    }
}
