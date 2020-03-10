package naitsirc98.beryl.graphics.textures;

import naitsirc98.beryl.images.PixelFormat;
import org.lwjgl.system.NativeResource;

public interface Texture extends NativeResource {

    Type type();

    PixelFormat internalFormat();

    PixelFormat format();

    void generateMipmaps();

    WrapMode wrapModeS();
    WrapMode wrapModeT();
    WrapMode wrapModeR();

    MinFilter minFilter();
    MagFilter magFilter();

    void samplerInfo(SamplerInfo samplerInfo);

    enum Type {
        TEXTURE_2D
        // TODO
    }

    enum WrapMode {

        REPEAT,
        MIRRORED_REPEAT,
        CLAMP_TO_BORDER,
        CLAMP_TO_EDGE
    }

    enum MinFilter {
        NEAREST_MIPMAP_NEAREST,
        NEAREST_MIPMAP_LINEAR,
        LINEAR_MIPMAP_NEAREST,
        LINEAR_MIPMAP_LINEAR
    }

    enum MagFilter {
        NEAREST,
        LINEAR
    }
}
