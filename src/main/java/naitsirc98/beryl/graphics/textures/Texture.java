package naitsirc98.beryl.graphics.textures;

import naitsirc98.beryl.images.PixelFormat;
import org.lwjgl.system.NativeResource;

public interface Texture extends NativeResource {

    Type type();

    Sampler sampler();

    PixelFormat internalFormat();

    PixelFormat format();

    void generateMipmaps();

    enum Type {
        TEXTURE_2D
        // TODO
    }
}
