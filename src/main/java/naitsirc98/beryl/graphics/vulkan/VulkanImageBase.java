package naitsirc98.beryl.graphics.vulkan;

import naitsirc98.beryl.graphics.vulkan.devices.VulkanLogicalDevice;
import naitsirc98.beryl.util.Destructor;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkImageCreateInfo;

@Destructor
public interface VulkanImageBase extends NativeResource {

    long vkImage();

    long vkImageView();

    VulkanLogicalDevice logicalDevice();

    int format();

}
