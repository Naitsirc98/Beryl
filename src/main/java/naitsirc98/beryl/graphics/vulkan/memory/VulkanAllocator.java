package naitsirc98.beryl.graphics.vulkan.memory;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanLogicalDevice;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanPhysicalDevice;
import naitsirc98.beryl.graphics.vulkan.textures.VulkanImage;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocationInfo;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkInstance;

import java.nio.LongBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;

public class VulkanAllocator implements VulkanObject.Long {

    private long vmaAllocator;

    public VulkanAllocator(VkInstance vkInstance, VulkanPhysicalDevice physicalDevice, VulkanLogicalDevice logicalDevice) {
        vmaAllocator = createVmaAllocator(vkInstance, physicalDevice, logicalDevice);
    }

    public VmaBufferAllocation createBuffer(VkBufferCreateInfo bufferCreateInfo, VmaAllocationCreateInfo allocationCreateInfo) {

        try(MemoryStack stack = stackPush()) {

            LongBuffer vkBuffer = stack.mallocLong(1);
            PointerBuffer vmaAllocation = stack.mallocPointer(1);

            vkCall(vmaCreateBuffer(vmaAllocator, bufferCreateInfo, allocationCreateInfo, vkBuffer, vmaAllocation, null));

            return new VmaBufferAllocation(vmaAllocation.get(0), allocationCreateInfo, vkBuffer.get(0), bufferCreateInfo);
        }
    }

    public void destroyBuffer(long vkBuffer, long allocation) {
        vmaDestroyBuffer(vmaAllocator, vkBuffer, allocation);
    }

    public VmaImageAllocation createImage(VkImageCreateInfo imageCreateInfo, VmaAllocationCreateInfo allocationCreateInfo) {

        try(MemoryStack stack = stackPush()) {

            LongBuffer vkImage = stack.mallocLong(1);
            PointerBuffer vmaAllocation = stack.mallocPointer(1);

            vkCall(vmaCreateImage(vmaAllocator, imageCreateInfo, allocationCreateInfo, vkImage, vmaAllocation, null));

            return new VmaImageAllocation(vmaAllocation.get(0), allocationCreateInfo, vkImage.get(0), imageCreateInfo);
        }
    }

    public void destroyImage(long vkImage, long allocation) {
        vmaDestroyImage(vmaAllocator, vkImage, allocation);
    }

    private long createVmaAllocator(VkInstance vkInstance, VulkanPhysicalDevice physicalDevice, VulkanLogicalDevice logicalDevice) {

        try(MemoryStack stack = stackPush()) {

            VmaAllocatorCreateInfo allocatorCreateInfo = VmaAllocatorCreateInfo.callocStack(stack)
                    .physicalDevice(physicalDevice.handle())
                    .device(logicalDevice.handle())
                    .pVulkanFunctions(VmaVulkanFunctions.callocStack(stack).set(vkInstance, logicalDevice.handle()));

            PointerBuffer pAllocator = stack.mallocPointer(1);

            vkCall(vmaCreateAllocator(allocatorCreateInfo, pAllocator));

            return pAllocator.get(0);
        }
    }

    @Override
    public long handle() {
        return vmaAllocator;
    }

    @Override
    public void free() {
        vmaDestroyAllocator(vmaAllocator);
        vmaAllocator = VK_NULL_HANDLE;
    }
}
