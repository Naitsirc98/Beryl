package naitsirc98.beryl.graphics.vulkan;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.vulkan.commands.VulkanCommandPool;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanLogicalDevice;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanPhysicalDevice;
import naitsirc98.beryl.graphics.vulkan.memory.VulkanAllocator;
import naitsirc98.beryl.graphics.vulkan.rendering.VulkanRenderer;
import naitsirc98.beryl.graphics.vulkan.swapchain.VulkanSwapchain;
import naitsirc98.beryl.util.handles.Handle;
import naitsirc98.beryl.util.handles.IntHandle;
import naitsirc98.beryl.util.handles.LongHandle;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkInstance;


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

    default VulkanAllocator allocator() {
        return vulkan().allocator();
    }

    default VulkanSwapchain swapchain() {
        return vulkan().swapchain();
    }

    default VulkanCommandPool graphicsCommandPool() {
        return vulkan().graphicsCommandPool();
    }

    default VulkanMapper mapper() {
        return Graphics.vulkan().mapper();
    }

    interface Long extends VulkanObject, LongHandle {

    }

    interface Custom<T> extends VulkanObject, Handle<T> {


    }
}
