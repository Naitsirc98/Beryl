package naitsirc98.beryl.graphics.vulkan.util;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.types.DataType;
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

    public static int asVkFormat(DataType dataType, int size) {

        switch(dataType) {
            case INT8:
                return int8VkFormat(size);
            case UINT8:
                return uint8VkFormat(size);
            case INT16:
                return int16VkFormat(size);
            case UINT16:
                return uint16VkFormat(size);
            case INT32:
                return int32VkFormat(size);
            case UINT32:
                return uint32VkFormat(size);
            case INT64:
                return int64VkFormat(size);
            case UINT64:
                return uint64VkFormat(size);
            case FLOAT16:
                return float16VkFormat(size);
            case FLOAT32:
                return float32VkFormat(size);
            case DOUBLE:
                break;
        }

        Log.error("Unknown Vulkan DataType: " + dataType);

        return VK_FORMAT_UNDEFINED;
    }

    private static int int8VkFormat(int size) {

        switch(size) {
            case 1:
                return VK_FORMAT_R8_SINT;
            case 2:
                return VK_FORMAT_R8G8_SINT;
            case 3:
                return VK_FORMAT_R8G8B8_SINT;
            case 4:
                return VK_FORMAT_R8G8B8A8_SINT;
        }

        Log.fatal("Illegal format size: " + size);

        return VK_FORMAT_UNDEFINED;
    }


    private static int uint8VkFormat(int size) {

        switch(size) {
            case 1:
                return VK_FORMAT_R8_UINT;
            case 2:
                return VK_FORMAT_R8G8_UINT;
            case 3:
                return VK_FORMAT_R8G8B8_UINT;
            case 4:
                return VK_FORMAT_R8G8B8A8_UINT;
        }

        Log.fatal("Illegal format size: " + size);

        return VK_FORMAT_UNDEFINED;
    }

    private static int int16VkFormat(int size) {

        switch(size) {
            case 1:
                return VK_FORMAT_R16_SINT;
            case 2:
                return VK_FORMAT_R16G16_SINT;
            case 3:
                return VK_FORMAT_R16G16B16_SINT;
            case 4:
                return VK_FORMAT_R16G16B16A16_SINT;
        }

        Log.fatal("Illegal format size: " + size);

        return VK_FORMAT_UNDEFINED;
    }


    private static int uint16VkFormat(int size) {

        switch(size) {
            case 1:
                return VK_FORMAT_R16_UINT;
            case 2:
                return VK_FORMAT_R16G16_UINT;
            case 3:
                return VK_FORMAT_R16G16B16_UINT;
            case 4:
                return VK_FORMAT_R16G16B16A16_UINT;
        }

        Log.fatal("Illegal format size: " + size);

        return VK_FORMAT_UNDEFINED;
    }

    private static int int32VkFormat(int size) {

        switch(size) {
            case 1:
                return VK_FORMAT_R32_SINT;
            case 2:
                return VK_FORMAT_R32G32_SINT;
            case 3:
                return VK_FORMAT_R32G32B32_SINT;
            case 4:
                return VK_FORMAT_R32G32B32A32_SINT;
        }

        Log.fatal("Illegal format size: " + size);

        return VK_FORMAT_UNDEFINED;
    }

    private static int uint32VkFormat(int size) {

        switch(size) {
            case 1:
                return VK_FORMAT_R32_UINT;
            case 2:
                return VK_FORMAT_R32G32_UINT;
            case 3:
                return VK_FORMAT_R32G32B32_UINT;
            case 4:
                return VK_FORMAT_R32G32B32A32_UINT;
        }

        Log.fatal("Illegal format size: " + size);

        return VK_FORMAT_UNDEFINED;
    }

    private static int int64VkFormat(int size) {

        switch(size) {
            case 1:
                return VK_FORMAT_R64_SINT;
            case 2:
                return VK_FORMAT_R64G64_SINT;
            case 3:
                return VK_FORMAT_R64G64B64_SINT;
            case 4:
                return VK_FORMAT_R64G64B64A64_SINT;
        }

        Log.fatal("Illegal format size: " + size);

        return VK_FORMAT_UNDEFINED;
    }

    private static int uint64VkFormat(int size) {

        switch(size) {
            case 1:
                return VK_FORMAT_R64_UINT;
            case 2:
                return VK_FORMAT_R64G64_UINT;
            case 3:
                return VK_FORMAT_R64G64B64_UINT;
            case 4:
                return VK_FORMAT_R64G64B64A64_UINT;
        }

        Log.fatal("Illegal format size: " + size);

        return VK_FORMAT_UNDEFINED;
    }

    private static int float16VkFormat(int size) {

        switch(size) {
            case 1:
                return VK_FORMAT_R16_SFLOAT;
            case 2:
                return VK_FORMAT_R16G16_SFLOAT;
            case 3:
                return VK_FORMAT_R16G16B16_SFLOAT;
            case 4:
                return VK_FORMAT_R16G16B16A16_SFLOAT;
        }

        Log.fatal("Illegal format size: " + size);

        return VK_FORMAT_UNDEFINED;
    }

    private static int float32VkFormat(int size) {

        switch(size) {
            case 1:
                return VK_FORMAT_R32_SFLOAT;
            case 2:
                return VK_FORMAT_R32G32_SFLOAT;
            case 3:
                return VK_FORMAT_R32G32B32_SFLOAT;
            case 4:
                return VK_FORMAT_R32G32B32A32_SFLOAT;
        }

        Log.fatal("Illegal format size: " + size);

        return VK_FORMAT_UNDEFINED;
    }


    private static int float64VkFormat(int size) {

        switch(size) {
            case 1:
                return VK_FORMAT_R64_SFLOAT;
            case 2:
                return VK_FORMAT_R64G64_SFLOAT;
            case 3:
                return VK_FORMAT_R64G64B64_SFLOAT;
            case 4:
                return VK_FORMAT_R64G64B64A64_SFLOAT;
        }

        Log.fatal("Illegal format size: " + size);

        return VK_FORMAT_UNDEFINED;
    }
}
