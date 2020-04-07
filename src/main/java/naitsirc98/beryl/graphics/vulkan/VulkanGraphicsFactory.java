package naitsirc98.beryl.graphics.vulkan;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.graphics.vulkan.textures.VulkanTexture2D;
import naitsirc98.beryl.images.Image;
import naitsirc98.beryl.images.ImageFactory;
import naitsirc98.beryl.images.PixelFormat;

public class VulkanGraphicsFactory implements GraphicsFactory {

    private VulkanTexture2D blankTexture2D;

    @Override
    public VulkanTexture2D newTexture2D() {
        return new VulkanTexture2D();
    }

    @Override
    public Texture2D blankTexture2D() {
        if(blankTexture2D == null) {
            blankTexture2D = newTexture2D();
            try(Image image = ImageFactory.newBlankImage(PixelFormat.RGBA)) {
                blankTexture2D.pixels(1, image);
            }
        }
        return blankTexture2D;
    }

    @Override
    public Texture2D newTexture2D(String imagePath, PixelFormat pixelFormat) {

        Texture2D texture = newTexture2D();

        try(Image image = ImageFactory.newBlankImage(PixelFormat.RGBA)) {
            texture.pixels(1, image);
        }

        return texture;
    }

    @Override
    public void release() {
        if(blankTexture2D != null) {
            blankTexture2D.release();
        }
    }
}
