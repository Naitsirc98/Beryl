package naitsirc98.beryl.graphics.opengl.textures;

import naitsirc98.beryl.graphics.opengl.GLObject;
import naitsirc98.beryl.graphics.textures.Sampler;
import org.lwjgl.system.MemoryStack;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class GLSampler implements GLObject, Sampler {

    private int handle;
    private BorderColor borderColor;

    public GLSampler() {
        handle = glCreateSamplers();
    }

    @Override
    public int handle() {
        return handle;
    }

    public void bind(int unit) {
        glBindSampler(unit, handle);
    }

    @Override
    public WrapMode wrapModeS() {
        return mapFromAPI(WrapMode.class, glGetSamplerParameteri(handle, GL_TEXTURE_WRAP_S));
    }

    @Override
    public Sampler wrapModeS(WrapMode wrapMode) {
        glSamplerParameteri(handle, GL_TEXTURE_WRAP_S, mapToAPI(wrapMode));
        return this;
    }

    @Override
    public WrapMode wrapModeT() {
        return mapFromAPI(WrapMode.class, glGetSamplerParameteri(handle, GL_TEXTURE_WRAP_T));
    }

    @Override
    public Sampler wrapModeT(WrapMode wrapMode) {
        glSamplerParameteri(handle, GL_TEXTURE_WRAP_T, mapToAPI(wrapMode));
        return this;
    }

    @Override
    public WrapMode wrapModeR() {
        return mapFromAPI(WrapMode.class, glGetSamplerParameteri(handle, GL_TEXTURE_WRAP_R));
    }

    @Override
    public Sampler wrapModeR(WrapMode wrapMode) {
        glSamplerParameteri(handle, GL_TEXTURE_WRAP_R, mapToAPI(wrapMode));
        return this;
    }

    @Override
    public MinFilter minFilter() {
        return mapFromAPI(MinFilter.class, glGetSamplerParameteri(handle, GL_TEXTURE_MIN_FILTER));
    }

    @Override
    public Sampler minFilter(MinFilter minFilter) {
        glSamplerParameteri(handle, GL_TEXTURE_MIN_FILTER, mapToAPI(minFilter));
        return this;
    }

    @Override
    public MagFilter magFilter() {
        return mapFromAPI(MagFilter.class, glGetSamplerParameteri(handle, GL_TEXTURE_MAG_FILTER));
    }

    @Override
    public Sampler magFilter(MagFilter magFilter) {
        glSamplerParameteri(handle, GL_TEXTURE_MAG_FILTER, mapToAPI(magFilter));
        return this;
    }

    @Override
    public float maxSupportedAnisotropy() {
        return glGetSamplerParameterf(handle, GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
    }

    @Override
    public float maxAnisotropy() {
        return glGetSamplerParameterf(handle, GL_TEXTURE_MAX_ANISOTROPY_EXT);
    }

    @Override
    public Sampler maxAnisotropy(float maxAnisotropy) {
        glSamplerParameterf(handle, GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy);
        return this;
    }

    @Override
    public boolean compareEnable() {
        return glGetSamplerParameteri(handle, GL_TEXTURE_COMPARE_MODE) == GL_COMPARE_REF_TO_TEXTURE;
    }

    @Override
    public Sampler compareEnable(boolean enable) {
        glSamplerParameteri(handle, GL_TEXTURE_COMPARE_MODE, enable ? GL_COMPARE_REF_TO_TEXTURE : GL_COMPARE_R_TO_TEXTURE);
        return this;
    }

    @Override
    public CompareOperation compareOperation() {
        return mapFromAPI(CompareOperation.class, glGetSamplerParameteri(handle, GL_TEXTURE_COMPARE_FUNC));
    }

    @Override
    public Sampler compareOperation(CompareOperation compareOperation) {
        glSamplerParameteri(handle, GL_TEXTURE_COMPARE_FUNC, mapToAPI(compareOperation));
        return this;
    }

    @Override
    public float minLod() {
        return glGetSamplerParameterf(handle, GL_TEXTURE_MIN_LOD);
    }

    @Override
    public Sampler minLod(float minLod) {
        glSamplerParameterf(handle, GL_TEXTURE_MIN_LOD, minLod);
        return this;
    }

    @Override
    public float maxLod() {
        return glGetSamplerParameterf(handle, GL_TEXTURE_MAX_LOD);
    }

    @Override
    public Sampler maxLod(float maxLod) {
        glSamplerParameterf(handle, GL_TEXTURE_MAX_LOD, maxLod);
        return this;
    }

    @Override
    public float lodBias() {
        return glGetSamplerParameterf(handle, GL_TEXTURE_LOD_BIAS);
    }

    @Override
    public Sampler lodBias(float lodBias) {
        glSamplerParameterf(handle, GL_TEXTURE_LOD_BIAS, lodBias);
        return this;
    }

    @Override
    public BorderColor borderColor() {
        return requireNonNull(borderColor);
    }

    @Override
    public Sampler borderColor(BorderColor borderColor) {
        this.borderColor = borderColor;
        setSamplerBorderColor(borderColor);
        return this;
    }

    @Override
    public void release() {
        glDeleteSamplers(handle);
        handle = NULL;
    }

    private void setSamplerBorderColor(BorderColor borderColor) {
        try(MemoryStack stack = stackPush()) {

            switch(borderColor) {
                case WHITE_INT_OPAQUE:
                    glSamplerParameteriv(handle, GL_TEXTURE_BORDER_COLOR, stack.ints(1, 1, 1, 1));
                    break;
                case BLACK_INT_OPAQUE:
                    glSamplerParameteriv(handle, GL_TEXTURE_BORDER_COLOR, stack.ints(0, 0, 0, 1));
                    break;
                case WHITE_FLOAT_OPAQUE:
                    glSamplerParameterfv(handle, GL_TEXTURE_BORDER_COLOR, stack.floats(1, 1, 1, 1));
                    break;
                case BLACK_FLOAT_OPAQUE:
                    glSamplerParameterfv(handle, GL_TEXTURE_BORDER_COLOR, stack.floats(0, 0, 0, 1));
                    break;
                case BLACK_INT_TRANSPARENT:
                    glSamplerParameteriv(handle, GL_TEXTURE_BORDER_COLOR, stack.ints(0, 0, 0, 0));
                    break;
                case BLACK_FLOAT_TRANSPARENT:
                    glSamplerParameterfv(handle, GL_TEXTURE_BORDER_COLOR, stack.floats(0, 0, 0, 0));
                    break;
            }
        }
    }
}
