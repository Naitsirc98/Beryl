package naitsirc98.beryl.graphics.vulkan.textures;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;

public interface VulkanImageBase extends VulkanObject.Long {

    default long vkImage() {
        return handle();
    }

    long vkImageView();

    int format();
}
