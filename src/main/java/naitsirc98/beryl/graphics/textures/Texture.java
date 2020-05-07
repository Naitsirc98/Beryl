package naitsirc98.beryl.graphics.textures;

import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.resources.Resource;
import naitsirc98.beryl.util.types.ByteSize;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static naitsirc98.beryl.graphics.textures.Sampler.MagFilter.LINEAR;
import static naitsirc98.beryl.graphics.textures.Sampler.MagFilter.NEAREST;
import static naitsirc98.beryl.graphics.textures.Sampler.MinFilter.*;
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

    int useCount();

    void incrementUseCount();

    void decrementUseCount();

    void resetUseCount();

    long residentHandle();

    long makeResident();

    void makeNonResident();

    void forceMakeNonResident();

    Sampler sampler();

    PixelFormat internalFormat();

    PixelFormat format();

    void generateMipmaps();

    default Texture setQuality(Quality quality) {

        switch(quality) {
            case LOW:
                setTextureLowQuality();
                break;
            case MEDIUM:
                setTextureMediumQuality();
                break;
            case HIGH:
                setTextureHighQuality();
                break;
            case VERY_HIGH:
                setTextureVeryHighQuality();
                break;
        }

        return this;
    }

    private void setTextureVeryHighQuality() {

        generateMipmaps();

        sampler().magFilter(LINEAR)
                .minFilter(LINEAR_MIPMAP_LINEAR)
                .lodBias(-2.0f)
                .maxAnisotropy(16.0f);
    }

    private void setTextureHighQuality() {

        generateMipmaps();

        sampler().magFilter(LINEAR)
                .minFilter(LINEAR_MIPMAP_LINEAR)
                .lodBias(-0.5f)
                .maxAnisotropy(4.0f);
    }

    private void setTextureMediumQuality() {

        generateMipmaps();

        sampler().magFilter(LINEAR)
                .minFilter(LINEAR_MIPMAP_NEAREST)
                .lodBias(0.0f)
                .maxAnisotropy(1.0f);
    }

    private void setTextureLowQuality() {
        sampler().magFilter(NEAREST)
                .minFilter(NEAREST_MIPMAP_NEAREST)
                .lodBias(1.0f)
                .maxAnisotropy(1.0f);
    }


    enum Quality {
        LOW,
        MEDIUM,
        HIGH,
        VERY_HIGH
    }
}
