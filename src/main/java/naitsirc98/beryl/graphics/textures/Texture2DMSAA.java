package naitsirc98.beryl.graphics.textures;

import naitsirc98.beryl.images.PixelFormat;

public interface Texture2DMSAA extends Texture {


    int width();
    int height();
    int samples();

    void allocate(int samples, int width, int height, PixelFormat internalFormat);

}
