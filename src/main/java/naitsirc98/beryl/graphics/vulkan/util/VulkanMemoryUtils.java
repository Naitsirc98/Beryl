package naitsirc98.beryl.graphics.vulkan.util;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.logging.Log;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import java.nio.LongBuffer;

import static naitsirc98.beryl.graphics.Graphics.vulkan;
import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;

public class VulkanMemoryUtils {

    public static long allocateMemoryFor(long vkBuffer, int desiredMemoryProperties) {

        try(MemoryStack stack = stackPush()) {

            VkMemoryRequirements memoryRequirements = VkMemoryRequirements.callocStack(stack);
            vkGetBufferMemoryRequirements(Graphics.vulkan().vkLogicalDevice(), vkBuffer, memoryRequirements);

            return allocateMemory(memoryRequirements.size(), memoryRequirements.memoryTypeBits(), desiredMemoryProperties);
        }
    }

    public static long allocateMemory(long size, int memoryTypeBits, int desiredMemoryProperties) {

        try(MemoryStack stack = stackPush()) {

            VkMemoryAllocateInfo allocateInfo = VkMemoryAllocateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                    .allocationSize(size)
                    .memoryTypeIndex(findVulkanMemoryType(memoryTypeBits, desiredMemoryProperties));

            LongBuffer pMemory = stack.mallocLong(1);
            vkCall(vkAllocateMemory(Graphics.vulkan().vkLogicalDevice(), allocateInfo, null, pMemory));

            return pMemory.get(0);
        }
    }

    public static int findVulkanMemoryType(int memoryTypeBits, int desiredProperties) {

        try(MemoryStack stack = stackPush()) {

            VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.mallocStack();
            vkGetPhysicalDeviceMemoryProperties(vulkan().device().physicalDevice().vkPhysicalDevice(), memProperties);

            for(int i = 0;i < memProperties.memoryTypeCount();i++) {
                if(isSuitableMemoryType(i, memoryTypeBits, memProperties.memoryTypes(i).propertyFlags(), desiredProperties)) {
                    return i;
                }
            }

            Log.fatal("Failed to find suitable memory type");

            return -1;
        }
    }

    private static boolean isSuitableMemoryType(int index, int memoryTypeBits, int memoryProperties, int desiredProperties) {
        return (memoryTypeBits & (1 << index)) != 0 && (memoryProperties & desiredProperties) == desiredProperties;
    }

}
