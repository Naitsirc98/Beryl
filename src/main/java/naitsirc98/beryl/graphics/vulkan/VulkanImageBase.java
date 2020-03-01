package naitsirc98.beryl.graphics.vulkan;

public interface VulkanImageBase extends VulkanObject.Long {

    default long vkImage() {
        return handle();
    }

    long vkImageView();

    int format();
}
