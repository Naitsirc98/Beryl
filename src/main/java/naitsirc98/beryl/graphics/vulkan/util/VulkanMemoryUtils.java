package naitsirc98.beryl.graphics.vulkan.util;

import naitsirc98.beryl.logging.Log;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import static naitsirc98.beryl.graphics.Graphics.vulkan;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceMemoryProperties;

public class VulkanMemoryUtils {

    public static int findVulkanMemoryType(int memoryTypeBits, int desiredProperties) {

        try(MemoryStack stack = stackPush()) {

            VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.mallocStack(stack);
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
