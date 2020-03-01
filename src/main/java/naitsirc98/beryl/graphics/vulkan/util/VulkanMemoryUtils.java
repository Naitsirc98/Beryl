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

public class VulkanMemoryUtils {

    public static long allocateVkBufferMemory(long vkBuffer, int desiredMemoryProperties) {

        try(MemoryStack stack = stackPush()) {

            VkMemoryRequirements.Buffer memoryRequirements = VkMemoryRequirements.callocStack(1, stack);
            vkGetBufferMemoryRequirements(Graphics.vulkan().logicalDevice().handle(), vkBuffer, memoryRequirements.get(0));

            return allocateVkBufferMemory(memoryRequirements, desiredMemoryProperties);
        }
    }

    public static long allocateVkBufferMemory(VkMemoryRequirements.Buffer memoryRequirements, int desiredMemoryProperties) {

        try(MemoryStack stack = stackPush()) {

            long size = 0;

            for(int i = 0;i < memoryRequirements.capacity();i++) {
                size += memoryRequirements.size();
            }

            VkMemoryAllocateInfo allocateInfo = VkMemoryAllocateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                    .allocationSize(size)
                    .memoryTypeIndex(findVulkanMemoryType(memoryRequirements.memoryTypeBits(), desiredMemoryProperties));

            LongBuffer pMemory = stack.mallocLong(1);
            vkCall(vkAllocateMemory(Graphics.vulkan().logicalDevice().handle(), allocateInfo, null, pMemory));

            return pMemory.get(0);
        }
    }

    public static int findVulkanMemoryType(int memoryTypeBits, int desiredProperties) {

        try(MemoryStack stack = stackPush()) {

            VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.mallocStack();
            vkGetPhysicalDeviceMemoryProperties(vulkan().physicalDevice().handle(), memProperties);

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
