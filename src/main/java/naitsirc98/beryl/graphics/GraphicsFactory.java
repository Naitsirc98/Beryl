package naitsirc98.beryl.graphics;

import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.resources.Resource;

public interface GraphicsFactory extends Resource {

    static GraphicsFactory get() {
        return Graphics.get().graphicsFactory();
    }

    Texture2D newTexture2D();

    Texture2D blankTexture2D();

    Texture2D newTexture2D(String imagePath, PixelFormat pixelFormat);
}
