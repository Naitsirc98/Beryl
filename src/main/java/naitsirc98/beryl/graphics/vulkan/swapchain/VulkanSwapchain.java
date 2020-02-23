package naitsirc98.beryl.graphics.vulkan.swapchain;

import naitsirc98.beryl.graphics.vulkan.devices.VulkanDevice;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanLogicalDevice;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanPhysicalDevice;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanPhysicalDevice.QueueFamilyIndices;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanPhysicalDevice.SwapChainSupportDetails;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.util.Destructor;
import naitsirc98.beryl.util.Sizec;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;

import static naitsirc98.beryl.util.DataType.UINT32_MAX;
import static naitsirc98.beryl.util.Maths.clamp;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

@Destructor
public final class VulkanSwapchain implements NativeResource {

    private final VulkanDevice device;
    private long swapchain;
    private int swapChainImageFormat;
    private VkExtent2D swapChainExtent;
    private VulkanSwapchainImage[] swapChainImages;

    public VulkanSwapchain(VulkanDevice device) {
        this.device = device;
        createSwapchain();
    }

    @Override
    public void free() {

        Arrays.stream(swapChainImages).forEach(VulkanSwapchainImage::free);

        swapChainExtent.free();

        vkDestroySwapchainKHR(device.logicalDevice().vkDevice(), swapchain, null);
    }

    private void createSwapchain() {

        try(MemoryStack stack = stackPush()) {

            VulkanPhysicalDevice physicalDevice = device.physicalDevice();
            VulkanLogicalDevice logicalDevice = device.logicalDevice();
            long surface = device.surface();

            SwapChainSupportDetails swapChainSupport = physicalDevice.swapChainSupportDetails();

            VkSurfaceFormatKHR surfaceFormat = chooseSwapSurfaceFormat(swapChainSupport.formats());
            int presentMode = chooseSwapPresentMode(swapChainSupport.presentModes());
            VkExtent2D extent = chooseSwapExtent(swapChainSupport.capabilities());

            IntBuffer imageCount = stack.ints(swapChainSupport.capabilities().minImageCount() + 1);

            if(swapChainSupport.capabilities().maxImageCount() > 0 && imageCount.get(0) > swapChainSupport.capabilities().maxImageCount()) {
                imageCount.put(0, swapChainSupport.capabilities().maxImageCount());
            }

            VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.callocStack(stack);

            createInfo.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
            createInfo.surface(surface);

            // Image settings
            createInfo.minImageCount(imageCount.get(0));
            createInfo.imageFormat(surfaceFormat.format());
            createInfo.imageColorSpace(surfaceFormat.colorSpace());
            createInfo.imageExtent(extent);
            createInfo.imageArrayLayers(1);
            createInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);

            QueueFamilyIndices queueFamilyIndices = physicalDevice.queueFamilyIndices();

            if(queueFamilyIndices.graphicsFamily() != queueFamilyIndices.presentationFamily()) {

                createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
                createInfo.pQueueFamilyIndices(stack.ints(queueFamilyIndices.graphicsFamily(), queueFamilyIndices.presentationFamily()));

            } else {
                createInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
            }

            createInfo.preTransform(swapChainSupport.capabilities().currentTransform());
            createInfo.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
            createInfo.presentMode(presentMode);
            createInfo.clipped(true);

            createInfo.oldSwapchain(VK_NULL_HANDLE);

            LongBuffer pSwapChain = stack.longs(VK_NULL_HANDLE);

            if(vkCreateSwapchainKHR(logicalDevice.vkDevice(), createInfo, null, pSwapChain) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create swap chain");
            }

            swapchain = pSwapChain.get(0);

            swapChainImageFormat = surfaceFormat.format();

            swapChainExtent = VkExtent2D.malloc().set(extent);

            getSwapchainImages(swapchain, imageCount, stack);

        }
    }

    private void getSwapchainImages(long swapchain, IntBuffer imageCount, MemoryStack stack) {

        vkGetSwapchainImagesKHR(device.logicalDevice().vkDevice(), swapchain, imageCount, null);

        LongBuffer pSwapchainImages = stack.mallocLong(imageCount.get(0));

        vkGetSwapchainImagesKHR(device.logicalDevice().vkDevice(), swapchain, imageCount, pSwapchainImages);

        swapChainImages = new VulkanSwapchainImage[imageCount.get(0)];

        for(int i = 0;i < pSwapchainImages.capacity();i++) {
            swapChainImages[i] = new VulkanSwapchainImage(device.logicalDevice(), pSwapchainImages.get(i), swapChainImageFormat);
        }
    }

    private VkSurfaceFormatKHR chooseSwapSurfaceFormat(VkSurfaceFormatKHR.Buffer availableFormats) {
        return availableFormats.stream()
                .filter(availableFormat -> availableFormat.format() == VK_FORMAT_B8G8R8_SRGB)
                .filter(availableFormat -> availableFormat.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
                .findAny()
                .orElse(availableFormats.get(0));
    }

    private int chooseSwapPresentMode(IntBuffer availablePresentModes) {

        for(int i = 0;i < availablePresentModes.capacity();i++) {
            if(availablePresentModes.get(i) == VK_PRESENT_MODE_MAILBOX_KHR) {
                return availablePresentModes.get(i);
            }
        }

        return VK_PRESENT_MODE_FIFO_KHR;
    }

    private VkExtent2D chooseSwapExtent(VkSurfaceCapabilitiesKHR capabilities) {

        if(capabilities.currentExtent().width() != UINT32_MAX) {
            return capabilities.currentExtent();
        }

        Sizec framebufferSize = Window.get().framebufferSize();

        VkExtent2D actualExtent = VkExtent2D.mallocStack().set(framebufferSize.width(), framebufferSize.height());

        VkExtent2D minExtent = capabilities.minImageExtent();
        VkExtent2D maxExtent = capabilities.maxImageExtent();

        actualExtent.width(clamp(minExtent.width(), maxExtent.width(), actualExtent.width()));
        actualExtent.height(clamp(minExtent.height(), maxExtent.height(), actualExtent.height()));

        return actualExtent;
    }

}
