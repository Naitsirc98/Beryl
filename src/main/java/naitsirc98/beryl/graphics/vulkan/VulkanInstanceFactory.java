package naitsirc98.beryl.graphics.vulkan;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.graphics.GraphicsAPI;
import naitsirc98.beryl.logging.Log;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;

import java.util.Collection;

import static naitsirc98.beryl.graphics.vulkan.VulkanContext.VULKAN_DEBUG_MESSAGES_ENABLED;
import static naitsirc98.beryl.graphics.vulkan.VulkanContext.VALIDATION_LAYERS;
import static naitsirc98.beryl.graphics.vulkan.VulkanDebugMessenger.newVulkanDebugMessengerCreateInfo;
import static naitsirc98.beryl.graphics.vulkan.VulkanExtensions.requiredExtensions;
import static naitsirc98.beryl.graphics.vulkan.VulkanUtils.makeVersion;
import static naitsirc98.beryl.graphics.vulkan.VulkanUtils.vkCall;
import static naitsirc98.beryl.graphics.vulkan.VulkanValidationLayers.*;
import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK11.VK_API_VERSION_1_1;

class VulkanInstanceFactory {

    public static VkInstance newVkInstance() {

        if(VULKAN_DEBUG_MESSAGES_ENABLED && !validationLayersSupported()) {
            Log.fatal("Validation layers requested but not supported");
            return null;
        }

        try(MemoryStack stack = stackPush()) {

            VkInstanceCreateInfo createInfo = newInstanceCreateInfo(stack, newApplicationInfo(stack));

            PointerBuffer pInstance = stack.mallocPointer(1);

            vkCall(vkCreateInstance(createInfo, null, pInstance));

            return new VkInstance(pInstance.get(0), createInfo);
        }
    }

    private static VkInstanceCreateInfo newInstanceCreateInfo(MemoryStack stack, VkApplicationInfo appInfo) {

        VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.callocStack(stack)
                .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                .pApplicationInfo(appInfo)
                .ppEnabledExtensionNames(requiredExtensions());

        if(VULKAN_DEBUG_MESSAGES_ENABLED) {
            createInfo.ppEnabledLayerNames(asPointerBuffer(VALIDATION_LAYERS));
            createInfo.pNext(newVulkanDebugMessengerCreateInfo(stack).address());
        }

        return createInfo;
    }

    public static PointerBuffer asPointerBuffer(Collection<String> values) {

        MemoryStack stack = stackGet();

        PointerBuffer buffer = stack.mallocPointer(values.size());

        values.stream()
                .map(stack::UTF8)
                .forEach(buffer::put);

        return buffer.rewind();
    }

    private static VkApplicationInfo newApplicationInfo(MemoryStack stack) {

        return VkApplicationInfo.callocStack(stack)
                .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
                .pApplicationName(stack.UTF8(Beryl.APPLICATION_NAME))
                .applicationVersion(makeVersion(Beryl.APPLICATION_VERSION))
                .pEngineName(stack.UTF8(Beryl.NAME))
                .engineVersion(makeVersion(Beryl.VERSION))
                .apiVersion(vulkanVersion());
    }

    private static int vulkanVersion() {
        return GraphicsAPI.VULKAN.versionMinor() == 1 ? VK_API_VERSION_1_1 : VK_API_VERSION_1_0;
    }

}
