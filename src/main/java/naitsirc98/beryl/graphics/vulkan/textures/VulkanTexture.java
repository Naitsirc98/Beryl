package naitsirc98.beryl.graphics.vulkan.textures;

import naitsirc98.beryl.graphics.textures.SamplerInfo;
import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.images.PixelFormat;

public abstract class VulkanTexture implements VulkanObject, Texture {



    @Override
    public PixelFormat internalFormat() {
        return null;
    }

    @Override
    public PixelFormat format() {
        return null;
    }

    @Override
    public void generateMipmaps() {

    }

    @Override
    public WrapMode wrapModeS() {
        return null;
    }

    @Override
    public WrapMode wrapModeT() {
        return null;
    }

    @Override
    public WrapMode wrapModeR() {
        return null;
    }

    @Override
    public Filter minFilter() {
        return null;
    }

    @Override
    public Filter magFilter() {
        return null;
    }

    @Override
    public void samplerInfo(SamplerInfo samplerInfo) {

    }

    @Override
    public void free() {

    }
}
