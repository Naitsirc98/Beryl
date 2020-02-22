package naitsirc98.beryl.graphics.vulkan;

import naitsirc98.beryl.logging.Log;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkInstance;

import java.nio.LongBuffer;

import static naitsirc98.beryl.graphics.vulkan.VulkanContext.VULKAN_DEBUG_MESSAGES_ENABLED;
import static naitsirc98.beryl.graphics.vulkan.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.VK10.*;


public class VulkanDebugMessenger implements NativeResource {

    static VulkanDebugMessenger newVulkanDebugMessenger(VkInstance vkInstance) {
        return VULKAN_DEBUG_MESSAGES_ENABLED ? new VulkanDebugMessenger(vkInstance) : null;
    }

    public static int vulkanDebugCallback(int messageSeverity, int messageType, long pCallbackData, long pUserData) {

        VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);

        Log.log(asLogLevel(messageSeverity), String.format("[VULKAN][%s]: %s", asString(messageType), callbackData.pMessageString()));

        return VK_FALSE;
    }

    public static VkDebugUtilsMessengerCreateInfoEXT newVulkanDebugMessengerCreateInfo(MemoryStack stack) {

        return VkDebugUtilsMessengerCreateInfoEXT.callocStack(stack)
            .sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
            .messageSeverity(severityBits())
            .messageType(typeBits())
            .pfnUserCallback(VulkanDebugMessenger::vulkanDebugCallback);
    }

    private final VkInstance vkInstance;
    private final VkDebugUtilsMessengerCallbackEXT debugCallback;
    private final long debugMessengerHandle;

    private VulkanDebugMessenger(VkInstance vkInstance) {
        this.vkInstance = vkInstance;
        debugCallback = VkDebugUtilsMessengerCallbackEXT.create(VulkanDebugMessenger::vulkanDebugCallback);
        debugMessengerHandle = createDebugMessenger();
    }

    private long createDebugMessenger() {

        try(MemoryStack stack = stackPush()) {

            LongBuffer pDebugMessenger = stack.longs(VK_NULL_HANDLE);

            vkCall(createDebugMessenger(newVulkanDebugMessengerCreateInfo(stack), pDebugMessenger));

            return pDebugMessenger.get(0);
        }
    }

    @Override
    public void free() {
        debugCallback.free();
        destroyDebugMessenger();
    }

    private int createDebugMessenger(VkDebugUtilsMessengerCreateInfoEXT createInfo, LongBuffer pDebugMessenger) {

        if(vkGetInstanceProcAddr(vkInstance, "vkCreateDebugUtilsMessengerEXT") != NULL) {
            return vkCreateDebugUtilsMessengerEXT(vkInstance, createInfo, null, pDebugMessenger);
        }

        return VK_ERROR_EXTENSION_NOT_PRESENT;
    }

    private void destroyDebugMessenger() {

        if(vkGetInstanceProcAddr(vkInstance, "vkDestroyDebugUtilsMessengerEXT") != NULL) {
            vkDestroyDebugUtilsMessengerEXT(vkInstance, debugMessengerHandle, null);
        }
    }

    private static int typeBits() {
        return VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT
                | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT
                | VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;
    }

    private static int severityBits() {
        return VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT
                | VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT
                | VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT
                | VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT;
    }

    private static String asString(int messageType) {
        switch(messageType) {
            case VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT:
                return "PERFORMANCE";
            case VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT:
                return "VALIDATION";
            default:
                return "GENERAL";
        }
    }

    private static Log.Level asLogLevel(int messageSeverity) {
        switch(messageSeverity) {
            case VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT:
                return Log.Level.INFO;
            case VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT:
                return Log.Level.WARNING;
            case VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT:
                return Log.Level.ERROR;
            default:
                return Log.Level.DEBUG;
        }
    }

}
