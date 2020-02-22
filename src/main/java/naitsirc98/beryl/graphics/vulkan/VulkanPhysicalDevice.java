package naitsirc98.beryl.graphics.vulkan;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.Pair;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;
import static naitsirc98.beryl.graphics.vulkan.VulkanContext.DEVICE_EXTENSIONS;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanPhysicalDevice {

    static VulkanPhysicalDevice pickPhysicalDevice(VkInstance vkInstance, long surface) {
        return new VulkanPhysicalDevice(vkInstance, surface);
    }

    private VkPhysicalDevice vkPhysicalDevice;
    private QueueFamilyIndices queueFamilyIndices;

    private VulkanPhysicalDevice(VkInstance vkInstance, long surface) {
        findSuitablePhysicalDevice(vkInstance, surface).get((physicalDevice, queueFamilyIndices) -> {
            vkPhysicalDevice = physicalDevice;
            this.queueFamilyIndices = queueFamilyIndices;
        });
    }

    public VkPhysicalDevice vkPhysicalDevice() {
        return vkPhysicalDevice;
    }

    public QueueFamilyIndices queueFamilyIndices() {
        return queueFamilyIndices;
    }

    private Pair<VkPhysicalDevice, QueueFamilyIndices> findSuitablePhysicalDevice(VkInstance vkInstance, long surface) {

        try(MemoryStack stack = stackPush()) {

            IntBuffer deviceCount = stack.ints(0);

            vkEnumeratePhysicalDevices(vkInstance, deviceCount, null);

            if(deviceCount.get(0) == 0) {
                Log.fatal("Failed to find GPUs with Vulkan support");
                return null;
            }

            PointerBuffer ppPhysicalDevices = stack.mallocPointer(deviceCount.get(0));

            vkEnumeratePhysicalDevices(vkInstance, deviceCount, ppPhysicalDevices);

            VkPhysicalDevice physicalDevice = null;
            QueueFamilyIndices queueFamilyIndices = null;

            for(int i = 0;i < ppPhysicalDevices.capacity();i++) {

                physicalDevice = new VkPhysicalDevice(ppPhysicalDevices.get(i), vkInstance);

                queueFamilyIndices = findQueueFamilies(physicalDevice, surface);

                if(isDeviceSuitable(physicalDevice, surface, queueFamilyIndices)) {
                    break;
                }
            }

            if(physicalDevice == null) {
                throw new RuntimeException("Failed to find a suitable GPU");
            }

            return new Pair<>(physicalDevice, queueFamilyIndices);
        }
    }

    public static QueueFamilyIndices findQueueFamilies(VkPhysicalDevice device, long surface) {

        QueueFamilyIndices indices = new QueueFamilyIndices();

        try(MemoryStack stack = stackPush()) {

            IntBuffer queueFamilyCount = stack.ints(0);

            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, null);

            VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.mallocStack(queueFamilyCount.get(0), stack);

            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, queueFamilies);

            IntBuffer presentSupport = stack.ints(VK_FALSE);

            for(int i = 0;i < queueFamilies.capacity() || !indices.isComplete();i++) {

                if((queueFamilies.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                    indices.graphicsFamily = i;
                }

                vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, presentSupport);

                if(presentSupport.get(0) == VK_TRUE) {
                    indices.presentationFamily = i;
                }
            }

            return indices;
        }
    }


    private static boolean isDeviceSuitable(VkPhysicalDevice device, long surface, QueueFamilyIndices queueFamilies) {

        boolean extensionsSupported = checkDeviceExtensionSupport(device);
        boolean swapChainAdequate = false;
        boolean anisotropySupported = false;

        if(extensionsSupported) {
            try(MemoryStack stack = stackPush()) {
                SwapChainSupportDetails swapChainSupport = querySwapChainSupport(device, surface, stack);
                swapChainAdequate = swapChainSupport.formats.hasRemaining() && swapChainSupport.presentModes.hasRemaining();
                VkPhysicalDeviceFeatures supportedFeatures = VkPhysicalDeviceFeatures.mallocStack(stack);
                vkGetPhysicalDeviceFeatures(device, supportedFeatures);
                anisotropySupported = supportedFeatures.samplerAnisotropy();
            }
        }

        return queueFamilies.isComplete() && extensionsSupported && swapChainAdequate && anisotropySupported;
    }

    private static boolean checkDeviceExtensionSupport(VkPhysicalDevice device) {

        try(MemoryStack stack = stackPush()) {

            IntBuffer extensionCount = stack.ints(0);

            vkEnumerateDeviceExtensionProperties(device, (String)null, extensionCount, null);

            VkExtensionProperties.Buffer availableExtensions = VkExtensionProperties.mallocStack(extensionCount.get(0), stack);

            return availableExtensions.stream().collect(toSet()).containsAll(DEVICE_EXTENSIONS);
        }
    }

    private static SwapChainSupportDetails querySwapChainSupport(VkPhysicalDevice device, long surface, MemoryStack stack) {

        SwapChainSupportDetails details = new SwapChainSupportDetails();

        details.capabilities = VkSurfaceCapabilitiesKHR.mallocStack(stack);
        vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, details.capabilities);

        IntBuffer count = stack.ints(0);

        vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, null);

        if(count.get(0) != 0) {
            details.formats = VkSurfaceFormatKHR.mallocStack(count.get(0), stack);
            vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, details.formats);
        }

        vkGetPhysicalDeviceSurfacePresentModesKHR(device,surface, count, null);

        if(count.get(0) != 0) {
            details.presentModes = stack.mallocInt(count.get(0));
            vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, count, details.presentModes);
        }

        return details;
    }

    private static class SwapChainSupportDetails {

        private VkSurfaceCapabilitiesKHR capabilities;
        private VkSurfaceFormatKHR.Buffer formats;
        private IntBuffer presentModes;

    }

    public static class QueueFamilyIndices {

        private Integer graphicsFamily;
        private Integer presentationFamily;

        public int graphicsFamily() {
            return graphicsFamily;
        }

        public int presentationFamily() {
            return presentationFamily;
        }

        public boolean isComplete() {
            return graphicsFamily != null && presentationFamily != null;
        }

        public int[] unique() {
            return IntStream.of(graphicsFamily, presentationFamily).distinct().toArray();
        }

        public int[] array() {
            return new int[] {graphicsFamily, presentationFamily};
        }
    }
}
