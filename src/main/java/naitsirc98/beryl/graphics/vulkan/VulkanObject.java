package naitsirc98.beryl.graphics.vulkan;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.vulkan.commands.VulkanCommandPool;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanLogicalDevice;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanPhysicalDevice;
import naitsirc98.beryl.util.Destructor;
import naitsirc98.beryl.util.Handle;
import naitsirc98.beryl.util.LongHandle;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkInstance;

@Destructor
public interface VulkanObject extends NativeResource {

    default VulkanContext vulkan() {
        return Graphics.vulkan();
    }

    default VkInstance vkInstance() {
        return vulkan().vkInstance();
    }

    default VulkanSurface surface() {
        return vulkan().surface();
    }

    default VulkanPhysicalDevice physicalDevice() {
        return vulkan().physicalDevice();
    }

    default VulkanLogicalDevice logicalDevice() {
        return vulkan().logicalDevice();
    }

    default VulkanCommandPool graphicsCommandPool() {
        return vulkan().graphicsCommandPool();
    }

    interface Long extends VulkanObject, LongHandle {

    }

    interface Vk<T> extends VulkanObject, Handle<T> {


    }
}
