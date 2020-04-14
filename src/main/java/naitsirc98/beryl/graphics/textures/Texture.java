package naitsirc98.beryl.graphics.textures;

import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.resources.Resource;
import naitsirc98.beryl.util.types.ByteSize;

import static java.lang.Math.max;
import static naitsirc98.beryl.util.Maths.log2;
import static naitsirc98.beryl.util.types.DataType.UINT64_SIZEOF;

@ByteSize.Static(Texture.SIZEOF)
public interface Texture extends Resource {

    static int calculateMipLevels(int width, int height) {
        return log2(max(width, height) + 1);
    }

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
