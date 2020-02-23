package naitsirc98.beryl.graphics.vulkan.util;

import naitsirc98.beryl.logging.Log;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkPhysicalDevice;

import java.nio.IntBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanFormatUtils {

    public static boolean formatHasStencilComponent(int format) {
        return format == VK_FORMAT_D32_SFLOAT_S8_UINT || format == VK_FORMAT_D24_UNORM_S8_UINT;
    }

    public static int findSupportedFormat(VkPhysicalDevice physicalDevice, IntBuffer formatCandidates, int tiling, int features) {

        try(MemoryStack stack = stackPush()) {

            VkFormatProperties props = VkFormatProperties.callocStack(stack);

            for(int i = 0; i < formatCandidates.capacity(); ++i) {

                int format = formatCandidates.get(i);

                vkGetPhysicalDeviceFormatProperties(physicalDevice, format, props);

                if(tiling == VK_IMAGE_TILING_LINEAR && (props.linearTilingFeatures() & features) == features) {
                    return format;
                } else if(tiling == VK_IMAGE_TILING_OPTIMAL && (props.optimalTilingFeatures() & features) == features) {
                    return format;
                }

            }
        }

        Log.fatal("Failed to find supported format");

        return -1;
    }


    public static int findDepthFormat(VkPhysicalDevice physicalDevice) {
        try(MemoryStack stack = stackPush()) {
            return findSupportedFormat(
                    physicalDevice,
                    stack.ints(VK_FORMAT_D32_SFLOAT, VK_FORMAT_D32_SFLOAT_S8_UINT, VK_FORMAT_D24_UNORM_S8_UINT),
                    VK_IMAGE_TILING_OPTIMAL,
                    VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT);
        }
    }

}
