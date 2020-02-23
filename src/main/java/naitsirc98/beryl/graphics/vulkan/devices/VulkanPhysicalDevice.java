package naitsirc98.beryl.graphics.vulkan.devices;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.Destructor;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;
import static naitsirc98.beryl.graphics.vulkan.VulkanContext.DEVICE_EXTENSIONS;
import static naitsirc98.beryl.graphics.vulkan.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.VK10.*;

@Destructor
public class VulkanPhysicalDevice implements NativeResource {

    static VulkanPhysicalDevice pickPhysicalDevice(VkInstance vkInstance, long surface) {
        return new VulkanPhysicalDevice(vkInstance, surface);
    }

    private final long surface;
    private final VkPhysicalDevice vkPhysicalDevice;
    private final QueueFamilyIndices queueFamilyIndices;
    private final SwapChainSupportDetails swapChainSupportDetails;

    private VulkanPhysicalDevice(VkInstance vkInstance, long surface) {
        this.surface = surface;

        PhysicalDeviceSelection selection = findSuitablePhysicalDevice(vkInstance);
        vkPhysicalDevice = selection.vkPhysicalDevice;
        queueFamilyIndices = selection.queueFamilyIndices;
        swapChainSupportDetails = selection.swapChainSupportDetails;
    }

    public VkPhysicalDevice vkPhysicalDevice() {
        return vkPhysicalDevice;
    }

    public QueueFamilyIndices queueFamilyIndices() {
        return queueFamilyIndices;
    }

    public SwapChainSupportDetails swapChainSupportDetails() {
        return swapChainSupportDetails;
    }

    @Override
    public void free() {
        swapChainSupportDetails.free();
    }

    private PhysicalDeviceSelection findSuitablePhysicalDevice(VkInstance vkInstance) {

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

                queueFamilyIndices = findQueueFamilies(physicalDevice);

                if(isDeviceSuitable(physicalDevice, queueFamilyIndices)) {
                    break;
                }
            }

            if(physicalDevice == null) {
                throw new RuntimeException("Failed to find a suitable GPU");
            }

            return new PhysicalDeviceSelection(physicalDevice, queueFamilyIndices, new SwapChainSupportDetails(physicalDevice));
        }
    }

    private QueueFamilyIndices findQueueFamilies(VkPhysicalDevice physicalDevice) {

        QueueFamilyIndices indices = new QueueFamilyIndices();

        try(MemoryStack stack = stackPush()) {

            IntBuffer queueFamilyCount = stack.ints(0);

            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, queueFamilyCount, null);

            VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.mallocStack(queueFamilyCount.get(0), stack);

            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, queueFamilyCount, queueFamilies);

            IntBuffer presentSupport = stack.ints(VK_FALSE);

            for(int i = 0;i < queueFamilies.capacity() || !indices.isComplete();i++) {

                if((queueFamilies.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                    indices.graphicsFamily = i;
                }

                vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, surface, presentSupport);

                if(presentSupport.get(0) == VK_TRUE) {
                    indices.presentationFamily = i;
                }
            }

            return indices;
        }
    }


    private boolean isDeviceSuitable(VkPhysicalDevice physicalDevice, QueueFamilyIndices queueFamilies) {

        boolean extensionsSupported = checkDeviceExtensionSupport(physicalDevice);
        boolean swapChainAdequate = false;
        boolean anisotropySupported = false;

        if(extensionsSupported) {
            try(MemoryStack stack = stackPush()) {

                SwapChainSupportDetails swapChainSupport = new SwapChainSupportDetails(physicalDevice, stack);
                swapChainAdequate = swapChainSupport.formats.hasRemaining() && swapChainSupport.presentModes.hasRemaining();

                VkPhysicalDeviceFeatures supportedFeatures = VkPhysicalDeviceFeatures.mallocStack(stack);
                vkGetPhysicalDeviceFeatures(physicalDevice, supportedFeatures);
                anisotropySupported = supportedFeatures.samplerAnisotropy();
            }
        }

        return queueFamilies.isComplete() && extensionsSupported && swapChainAdequate && anisotropySupported;
    }

    private boolean checkDeviceExtensionSupport(VkPhysicalDevice device) {

        try(MemoryStack stack = stackPush()) {

            IntBuffer extensionCount = stack.ints(0);

            vkEnumerateDeviceExtensionProperties(device, (String)null, extensionCount, null);

            VkExtensionProperties.Buffer availableExtensions = VkExtensionProperties.mallocStack(extensionCount.get(0), stack);

            return availableExtensions.stream().collect(toSet()).containsAll(DEVICE_EXTENSIONS);
        }
    }

    @Destructor
    public final class SwapChainSupportDetails implements NativeResource {

        private VkSurfaceCapabilitiesKHR capabilities;
        private VkSurfaceFormatKHR.Buffer formats;
        private IntBuffer presentModes;
        private final boolean stackAllocated;

        /**
         * Initializes a new SwapChainSupportDetails on the stack
         * */
        public SwapChainSupportDetails(VkPhysicalDevice vkPhysicalDevice, MemoryStack stack) {

            stackAllocated = true;

            capabilities = VkSurfaceCapabilitiesKHR.mallocStack(stack);

            IntBuffer formatsCount = stack.mallocInt(1);
            vkCall(vkGetPhysicalDeviceSurfaceFormatsKHR(vkPhysicalDevice, surface, formatsCount, null));

            formats = VkSurfaceFormatKHR.mallocStack(formatsCount.get(0), stack);

            IntBuffer presentModesCount = stack.mallocInt(1);
            vkCall(vkGetPhysicalDeviceSurfacePresentModesKHR(vkPhysicalDevice, surface, presentModesCount, null));

            presentModes = stack.mallocInt(presentModesCount.get(0));

            init(vkPhysicalDevice, formatsCount, presentModesCount);
        }

        /**
         * Initializes a new SwapChainSupportDetails dynamically
         * */
        public SwapChainSupportDetails(VkPhysicalDevice vkPhysicalDevice) {

            stackAllocated = false;

            capabilities = VkSurfaceCapabilitiesKHR.malloc();

            try(MemoryStack stack = stackPush()) {

                IntBuffer formatsCount = stack.mallocInt(1);
                vkCall(vkGetPhysicalDeviceSurfaceFormatsKHR(vkPhysicalDevice, surface, formatsCount, null));

                formats = VkSurfaceFormatKHR.malloc(formatsCount.get(0));

                IntBuffer presentModesCount = stack.mallocInt(1);
                vkCall(vkGetPhysicalDeviceSurfacePresentModesKHR(vkPhysicalDevice, surface, presentModesCount, null));

                presentModes = memAllocInt(presentModesCount.get(0));

                init(vkPhysicalDevice, formatsCount, presentModesCount);
            }
        }

        private void init(VkPhysicalDevice vkPhysicalDevice, IntBuffer formatsCount, IntBuffer presentModesCount) {

            vkCall(vkGetPhysicalDeviceSurfaceCapabilitiesKHR(vkPhysicalDevice, surface, capabilities));

            vkCall(vkGetPhysicalDeviceSurfaceFormatsKHR(vkPhysicalDevice, surface, formatsCount, formats));

            vkCall(vkGetPhysicalDeviceSurfacePresentModesKHR(vkPhysicalDevice, surface, presentModesCount, presentModes));
        }

        public VkSurfaceCapabilitiesKHR capabilities() {
            return capabilities;
        }

        public VkSurfaceFormatKHR.Buffer formats() {
            return formats;
        }

        public IntBuffer presentModes() {
            return presentModes;
        }

        @Override
        public void free() {
            if(!stackAllocated) {
                memFree(presentModes);
                capabilities.free();
                formats.free();
            }
        }
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

    private class PhysicalDeviceSelection {

        private final VkPhysicalDevice vkPhysicalDevice;
        private final QueueFamilyIndices queueFamilyIndices;
        private final SwapChainSupportDetails swapChainSupportDetails;

        public PhysicalDeviceSelection(VkPhysicalDevice vkPhysicalDevice, QueueFamilyIndices queueFamilyIndices,
                                       SwapChainSupportDetails swapChainSupportDetails) {
            this.vkPhysicalDevice = vkPhysicalDevice;
            this.queueFamilyIndices = queueFamilyIndices;
            this.swapChainSupportDetails = swapChainSupportDetails;
        }
    }

}
