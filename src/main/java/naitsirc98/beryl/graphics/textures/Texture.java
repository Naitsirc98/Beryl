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

    Filter minFilter();
    Filter magFilter();

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

    enum Filter {

        NEAREST,
        LINEAR,
        MIPMAP_NEAREST,
        MIPMAP_LINEAR
    }
}
