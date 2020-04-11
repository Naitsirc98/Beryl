package naitsirc98.beryl.graphics.textures;

import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.resources.Resource;
import naitsirc98.beryl.util.types.ByteSize;

import static naitsirc98.beryl.util.types.DataType.UINT64_SIZEOF;

@ByteSize.Static(Texture.SIZEOF)
public interface Texture extends Resource {

    /**
     * Size of a texture handle
     * */
    int SIZEOF = UINT64_SIZEOF;

    long residentHandle();

    long makeResident();

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
