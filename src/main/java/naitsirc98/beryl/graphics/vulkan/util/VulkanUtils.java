package naitsirc98.beryl.graphics.vulkan.util;

import naitsirc98.beryl.graphics.buffers.GraphicsBuffer;
import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.Version;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Pointer;

import java.util.Collection;

import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.vulkan.KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_SUBOPTIMAL_KHR;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanUtils {

    public static boolean vkCall(int vkResult) {
        if(vkResult != VK_SUCCESS) {
            Log.log(Log.Level.ERROR, getVulkanErrorName(vkResult));
        }
        return vkResult == VK_SUCCESS;
    }

    public static boolean vkCall(int vkResult, Log.Level levelIfFail) {
        if(vkResult != VK_SUCCESS) {
            Log.log(levelIfFail, getVulkanErrorName(vkResult));
        }
        return vkResult == VK_SUCCESS;
    }

    public static int makeVersion(Version version) {
        return VK_MAKE_VERSION(version.major(), version.minor(), version.revision());
    }

    public static PointerBuffer stringPointers(Collection<String> values) {

        MemoryStack stack = stackGet();

        PointerBuffer buffer = stack.mallocPointer(values.size());

        values.stream()
                .map(stack::UTF8)
                .forEach(buffer::put);

        return buffer.rewind();
    }

    public static PointerBuffer pointers(Pointer... pointers) {

        MemoryStack stack = stackGet();

        PointerBuffer buffer = stack.mallocPointer(pointers.length);

        for(Pointer pointer : pointers) {
            buffer.put(pointer);
        }

        return buffer.rewind();
    }

    public static GraphicsBuffer.Type vkToBufferType(int usage) {

        if((usage & VK_BUFFER_USAGE_VERTEX_BUFFER_BIT) == VK_BUFFER_USAGE_VERTEX_BUFFER_BIT) {
            return GraphicsBuffer.Type.VERTEX_BUFFER;
        } else if((usage & VK_BUFFER_USAGE_INDEX_BUFFER_BIT) == VK_BUFFER_USAGE_INDEX_BUFFER_BIT) {
            return GraphicsBuffer.Type.INDEX_BUFFER;
        } else if((usage & VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT) == VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT) {
            return GraphicsBuffer.Type.UNIFORM_BUFFER;
        }

        throw new IllegalArgumentException("Unknown Vulkan buffer usage: " + usage);
    }

    public static Texture.WrapMode vkToWrapMode(int vkAddressMode) {
        switch(vkAddressMode) {
            case VK_SAMPLER_ADDRESS_MODE_REPEAT:
                return Texture.WrapMode.REPEAT;
            case VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT:
                return Texture.WrapMode.MIRRORED_REPEAT;
            case VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE:
                return Texture.WrapMode.CLAMP_TO_EDGE;
            case VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER:
                return Texture.WrapMode.CLAMP_TO_BORDER;
        }
        throw new IllegalArgumentException("Unsupported Vulkan Address Mode: " + vkAddressMode);
    }

    public static Texture.MagFilter vkToMagFilter(int vkMagFilter) {

        if(vkMagFilter == VK_FILTER_NEAREST) {
            return Texture.MagFilter.NEAREST;
        } else if(vkMagFilter == VK_FILTER_LINEAR) {
            return Texture.MagFilter.LINEAR;
        }

        throw new IllegalArgumentException("Unknown Vulkan Mag Filter: " + vkMagFilter);
    }

    public static Texture.MinFilter vkToMinFilter(int vkMinFilter, int vkMipmapMode) {

        if(vkMinFilter == VK_FILTER_NEAREST) {

            if(vkMipmapMode == VK_SAMPLER_MIPMAP_MODE_NEAREST) {
                return Texture.MinFilter.NEAREST_MIPMAP_NEAREST;
            } else if(vkMipmapMode == VK_SAMPLER_MIPMAP_MODE_LINEAR) {
                return Texture.MinFilter.NEAREST_MIPMAP_LINEAR;
            }

        } else if(vkMinFilter == VK_FILTER_LINEAR) {

            if(vkMipmapMode == VK_SAMPLER_MIPMAP_MODE_NEAREST) {
                return Texture.MinFilter.LINEAR_MIPMAP_NEAREST;
            } else if(vkMipmapMode == VK_SAMPLER_MIPMAP_MODE_LINEAR) {
                return Texture.MinFilter.LINEAR_MIPMAP_LINEAR;
            }

        }

        throw new IllegalArgumentException("Unknown Vulkan Min Filter: " + vkMinFilter + ", " + vkMipmapMode);
    }

    public static String getVulkanErrorName(int errorCode) {
        switch(errorCode) {
            case VK_NOT_READY:
                return "VK_NOT_READY";
            case VK_TIMEOUT:
                return "VK_TIMEOUT";
            case VK_EVENT_SET:
                return "VK_EVENT_SET";
            case VK_EVENT_RESET:
                return "VK_EVENT_RESET";
            case VK_INCOMPLETE:
                return "VK_INCOMPLETE";
            case VK_ERROR_OUT_OF_HOST_MEMORY:
                return "VK_ERROR_OUT_OF_HOST_MEMORY";
            case VK_ERROR_OUT_OF_DEVICE_MEMORY:
                return "VK_ERROR_OUT_OF_DEVICE_MEMORY";
            case VK_ERROR_INITIALIZATION_FAILED:
                return "VK_ERROR_INITIALIZATION_FAILED";
            case VK_ERROR_DEVICE_LOST:
                return "VK_ERROR_DEVICE_LOST";
            case VK_ERROR_MEMORY_MAP_FAILED:
                return "VK_ERROR_MEMORY_MAP_FAILED";
            case VK_ERROR_LAYER_NOT_PRESENT:
                return "VK_ERROR_LAYER_NOT_PRESENT";
            case VK_ERROR_EXTENSION_NOT_PRESENT:
                return "VK_ERROR_EXTENSION_NOT_PRESENT";
            case VK_ERROR_FEATURE_NOT_PRESENT:
                return "VK_ERROR_FEATURE_NOT_PRESENT";
            case VK_ERROR_INCOMPATIBLE_DRIVER:
                return "VK_ERROR_INCOMPATIBLE_DRIVER";
            case VK_ERROR_TOO_MANY_OBJECTS:
                return "VK_ERROR_TOO_MANY_OBJECTS";
            case VK_ERROR_FORMAT_NOT_SUPPORTED:
                return "VK_ERROR_FORMAT_NOT_SUPPORTED";
            case VK_ERROR_FRAGMENTED_POOL:
                return "VK_ERROR_FRAGMENTED_POOL";
            case VK_SUBOPTIMAL_KHR:
                return "VK_SUBOPTIMAL_KHR";
            case VK_ERROR_OUT_OF_DATE_KHR:
                return "VK_ERROR_OUT_OF_DATE_KHR";
            default:
                return "Unknown Vulkan error code (" + errorCode + ")";
        }
    }
}
