package naitsirc98.beryl.graphics.vulkan.textures;

import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.logging.Log;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanFormatUtils.vkToPixelFormat;

public abstract class VulkanTexture implements VulkanObject, Texture {

    protected VulkanRenderImage renderImage;
    protected VulkanSampler sampler;

    public VulkanTexture() {
        renderImage = new VulkanRenderImage(new VulkanImage(), new VulkanImageView());
        this.sampler = new VulkanSampler();
    }

    public VulkanImage image() {
        return renderImage.image();
    }

    public VulkanImageView view() {
        return renderImage.view();
    }

    @Override
    public VulkanSampler sampler() {
        return sampler;
    }

    @Override
    public PixelFormat internalFormat() {
        return vkToPixelFormat(image().format());
    }

    @Override
    public PixelFormat format() {
        return vkToPixelFormat(view().format());
    }

    @Override
    public void generateMipmaps() {
        if(renderImage == null || renderImage.image() == null || renderImage.image().isNull()) {
            Log.error("Trying to generateMipmaps, but the texture image is null");
            return;
        }
        renderImage.image().generateMipmaps();
    }

    @Override
    public void free() {

        if(renderImage != null) {
            renderImage.free();
            renderImage = null;
        }

        sampler.free();
        sampler = null;
    }
}
