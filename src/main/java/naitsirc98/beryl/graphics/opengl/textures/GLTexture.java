package naitsirc98.beryl.graphics.opengl.textures;

import naitsirc98.beryl.graphics.opengl.GLObject;
import naitsirc98.beryl.graphics.textures.Sampler;
import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.resources.ManagedResource;
import org.lwjgl.system.MemoryStack;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.graphics.Graphics.opengl;
import static org.lwjgl.opengl.ARBBindlessTexture.*;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.GL_COMPARE_REF_TO_TEXTURE;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.opengl.GL45C.glBindTextureUnit;
import static org.lwjgl.system.MemoryStack.stackPush;

public abstract class GLTexture extends ManagedResource implements GLObject, Texture, Sampler {

    protected int handle;
    private long residentHandle;
    private BorderColor borderColor;

    public GLTexture(int target) {
        this.handle = glCreateTextures(target);
        // sampler().defaults();
    }

    @Override
    public int handle() {
        return handle;
    }

    @Override
    public long residentHandle() {
        return glGetTextureHandleARB(handle);
    }

    public long makeResident() {
        if(residentHandle == NULL) {
            residentHandle = residentHandle();
            glMakeTextureHandleResidentARB(residentHandle);
        }
        return residentHandle;
    }

    public void makeNonResident() {
        if(residentHandle != NULL) {
            glMakeTextureHandleNonResidentARB(residentHandle());
        }
    }

    @Override
    public Sampler sampler() {
        return this;
    }

    public void bind(int unit) {
        glBindTextureUnit(unit, handle());
    }

    public void unbind(int unit) {
        glBindTextureUnit(unit, 0);
    }

    @Override
    public WrapMode wrapModeS() {
        return mapFromAPI(WrapMode.class, glGetTextureParameteri(handle, GL_TEXTURE_WRAP_S));
    }

    @Override
    public Sampler wrapModeS(WrapMode wrapMode) {
        glTextureParameteri(handle, GL_TEXTURE_WRAP_S, mapToAPI(wrapMode));
        return this;
    }

    @Override
    public WrapMode wrapModeT() {
        return mapFromAPI(WrapMode.class, glGetTextureParameteri(handle, GL_TEXTURE_WRAP_T));
    }

    @Override
    public Sampler wrapModeT(WrapMode wrapMode) {
        glTextureParameteri(handle, GL_TEXTURE_WRAP_T, mapToAPI(wrapMode));
        return this;
    }

    @Override
    public WrapMode wrapModeR() {
        return mapFromAPI(WrapMode.class, glGetTextureParameteri(handle, GL_TEXTURE_WRAP_R));
    }

    @Override
    public Sampler wrapModeR(WrapMode wrapMode) {
        glTextureParameteri(handle, GL_TEXTURE_WRAP_R, mapToAPI(wrapMode));
        return this;
    }

    @Override
    public MinFilter minFilter() {
        return mapFromAPI(MinFilter.class, glGetTextureParameteri(handle, GL_TEXTURE_MIN_FILTER));
    }

    @Override
    public Sampler minFilter(MinFilter minFilter) {
        glTextureParameteri(handle, GL_TEXTURE_MIN_FILTER, mapToAPI(minFilter));
        return this;
    }

    @Override
    public MagFilter magFilter() {
        return mapFromAPI(MagFilter.class, glGetTextureParameteri(handle, GL_TEXTURE_MAG_FILTER));
    }

    @Override
    public Sampler magFilter(MagFilter magFilter) {
        glTextureParameteri(handle, GL_TEXTURE_MAG_FILTER, mapToAPI(magFilter));
        return this;
    }

    @Override
    public float maxSupportedAnisotropy() {
        if(opengl().capabilities().GL_EXT_texture_filter_anisotropic) {
            return glGetTextureParameterf(handle, GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
        }
        return 1.0f;
    }

    @Override
    public float maxAnisotropy() {
        if(opengl().capabilities().GL_EXT_texture_filter_anisotropic) {
            return glGetTextureParameterf(handle, GL_TEXTURE_MAX_ANISOTROPY_EXT);
        }
        return 1.0f;
    }

    @Override
    public Sampler maxAnisotropy(float maxAnisotropy) {
        if(opengl().capabilities().GL_EXT_texture_filter_anisotropic) {
            glTextureParameterf(handle, GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy);
        } else {
            Log.warning("Anisotropic filtering not supported in this device");
        }
        return this;
    }

    @Override
    public boolean compareEnable() {
        return glGetTextureParameteri(handle, GL_TEXTURE_COMPARE_MODE) == GL_COMPARE_REF_TO_TEXTURE;
    }

    @Override
    public Sampler compareEnable(boolean enable) {
        glTextureParameteri(handle, GL_TEXTURE_COMPARE_MODE, enable ? GL_COMPARE_REF_TO_TEXTURE : GL_COMPARE_R_TO_TEXTURE);
        return this;
    }

    @Override
    public CompareOperation compareOperation() {
        return mapFromAPI(CompareOperation.class, glGetTextureParameteri(handle, GL_TEXTURE_COMPARE_FUNC));
    }

    @Override
    public Sampler compareOperation(CompareOperation compareOperation) {
        glTextureParameteri(handle, GL_TEXTURE_COMPARE_FUNC, mapToAPI(compareOperation));
        return this;
    }

    @Override
    public float minLod() {
        return glGetTextureParameterf(handle, GL_TEXTURE_MIN_LOD);
    }

    @Override
    public Sampler minLod(float minLod) {
        glTextureParameterf(handle, GL_TEXTURE_MIN_LOD, minLod);
        return this;
    }

    @Override
    public float maxLod() {
        return glGetTextureParameterf(handle, GL_TEXTURE_MAX_LOD);
    }

    @Override
    public Sampler maxLod(float maxLod) {
        glTextureParameterf(handle, GL_TEXTURE_MAX_LOD, maxLod);
        return this;
    }

    @Override
    public float lodBias() {
        return glGetTextureParameterf(handle, GL_TEXTURE_LOD_BIAS);
    }

    @Override
    public Sampler lodBias(float lodBias) {
        glTextureParameterf(handle, GL_TEXTURE_LOD_BIAS, lodBias);
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
    protected void free() {
        makeNonResident();
        glDeleteTextures(handle);

        handle = NULL;
    }

    private void setSamplerBorderColor(BorderColor borderColor) {
        try (MemoryStack stack = stackPush()) {

            switch (borderColor) {
                case WHITE_INT_OPAQUE:
                    glTextureParameterIiv(handle, GL_TEXTURE_BORDER_COLOR, stack.ints(1, 1, 1, 1));
                    break;
                case BLACK_INT_OPAQUE:
                    glTextureParameterIiv(handle, GL_TEXTURE_BORDER_COLOR, stack.ints(0, 0, 0, 1));
                    break;
                case WHITE_FLOAT_OPAQUE:
                    glTextureParameterfv(handle, GL_TEXTURE_BORDER_COLOR, stack.floats(1, 1, 1, 1));
                    break;
                case BLACK_FLOAT_OPAQUE:
                    glTextureParameterfv(handle, GL_TEXTURE_BORDER_COLOR, stack.floats(0, 0, 0, 1));
                    break;
                case BLACK_INT_TRANSPARENT:
                    glTextureParameterIiv(handle, GL_TEXTURE_BORDER_COLOR, stack.ints(0, 0, 0, 0));
                    break;
                case BLACK_FLOAT_TRANSPARENT:
                    glTextureParameterfv(handle, GL_TEXTURE_BORDER_COLOR, stack.floats(0, 0, 0, 0));
                    break;
            }
        }
    }
}
