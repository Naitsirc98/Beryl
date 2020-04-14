package naitsirc98.beryl.graphics.opengl.textures;

import naitsirc98.beryl.graphics.textures.Texture2DMSAA;
import naitsirc98.beryl.images.PixelFormat;

import static org.lwjgl.opengl.GL32.GL_TEXTURE_2D_MULTISAMPLE;
import static org.lwjgl.opengl.GL45C.glTextureStorage2DMultisample;

public class GLTexture2DMSAA extends GLTexture implements Texture2DMSAA {

    private int samples;

    public GLTexture2DMSAA() {
        super(GL_TEXTURE_2D_MULTISAMPLE);
    }

    @Override
    public int samples() {
        return samples;
    }

    public void allocate(int samples, int width, int height, int internalFormat) {
        glTextureStorage2DMultisample(handle, samples, internalFormat, width, height, true);
        this.samples = samples;
    }

    @Override
    public void allocate(int samples, int width, int height, PixelFormat internalFormat) {
        glTextureStorage2DMultisample(handle, samples, mapper().mapToSizedInternalFormat(internalFormat), width, height, true);
        this.samples = samples;
    }
}
