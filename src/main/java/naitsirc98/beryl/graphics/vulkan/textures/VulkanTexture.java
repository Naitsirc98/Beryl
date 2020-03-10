package naitsirc98.beryl.graphics.vulkan.textures;

import naitsirc98.beryl.graphics.textures.SamplerInfo;
import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.images.PixelFormat;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanFormatUtils.vkToPixelFormat;
import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.*;

public abstract class VulkanTexture implements VulkanObject, Texture {

    private VulkanRenderImage renderImage;
    private VulkanSampler sampler;

    public VulkanTexture(VulkanRenderImage renderImage, VulkanSampler sampler) {
        this.renderImage = renderImage;
        this.sampler = sampler;
    }

    public VulkanImage image() {
        return renderImage.image();
    }

    public VulkanImageView view() {
        return renderImage.view();
    }

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
        // TODO
    }

    @Override
    public WrapMode wrapModeS() {
        return vkToWrapMode(sampler.addressModeU());
    }

    @Override
    public WrapMode wrapModeT() {
        return vkToWrapMode(sampler.addressModeV());
    }

    @Override
    public WrapMode wrapModeR() {
        return vkToWrapMode(sampler.addressModeW());
    }

    @Override
    public MinFilter minFilter() {
        return vkToMinFilter(sampler.minFilter(), sampler.mipmapMode());
    }

    @Override
    public MagFilter magFilter() {
        return vkToMagFilter(sampler.magFilter());
    }

    @Override
    public void samplerInfo(SamplerInfo samplerInfo) {
        // TODO
    }

    @Override
    public void free() {

        renderImage.free();
        sampler.free();

        renderImage = null;
        sampler = null;
    }
}
