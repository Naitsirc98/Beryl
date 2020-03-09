package naitsirc98.beryl.graphics.vulkan.textures;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;

public class VulkanTexture implements VulkanObject {

    private final VulkanImage image;
    private final long vkSampler;

    public VulkanTexture(VulkanImage image, long vkSampler) {
        this.image = image;
        this.vkSampler = vkSampler;
    }

    @Override
    public void free() {

    }
}
