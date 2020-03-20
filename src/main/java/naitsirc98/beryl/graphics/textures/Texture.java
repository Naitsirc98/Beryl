package naitsirc98.beryl.graphics.textures;

import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.resources.Resource;

public interface Texture extends Resource {

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
