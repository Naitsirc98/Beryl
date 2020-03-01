package naitsirc98.beryl.graphics.vulkan.swapchain;

import naitsirc98.beryl.graphics.vulkan.VulkanImage;
import naitsirc98.beryl.graphics.vulkan.VulkanImageBase;
import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanLogicalDevice;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanPhysicalDevice;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanPhysicalDevice.QueueFamilyIndices;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanPhysicalDevice.SwapChainSupportDetails;
import naitsirc98.beryl.graphics.vulkan.renderpasses.VulkanRenderPass;
import naitsirc98.beryl.graphics.vulkan.renderpasses.VulkanSubPassAttachments;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.util.types.Destructor;
import naitsirc98.beryl.util.geometry.Sizec;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanFormatUtils.findDepthFormat;
import static naitsirc98.beryl.util.types.DataType.UINT32_MAX;
import static naitsirc98.beryl.util.Maths.clamp;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

@Destructor
public class VulkanSwapchain implements VulkanObject.Long {

    // TODO: add MSAA support

    private static final int COLOR_ATTACHMENT_INDEX = 0;
    private static final int DEPTH_ATTACHMENT_INDEX = 1;

    private long vkSwapchain;
    private int swapChainImageFormat;
    private VkExtent2D swapChainExtent;
    private VulkanSwapchainImage[] swapChainImages;
    private VulkanImageBase depthImage;
    private VulkanRenderPass renderPass;

    public VulkanSwapchain() {
        createSwapchain();
        renderPass = createSwapchainRenderPass();
    }

    @Override
    public long handle() {
        return vkSwapchain;
    }

    public int swapChainImageFormat() {
        return swapChainImageFormat;
    }

    public VkExtent2D swapChainExtent() {
        return swapChainExtent;
    }

    public VulkanSwapchainImage[] swapChainImages() {
        return swapChainImages;
    }

    public VulkanImageBase depthImage() {
        return depthImage;
    }

    public VulkanRenderPass renderPass() {
        return renderPass;
    }


    @Override
    public void free() {

        depthImage.free();

        renderPass.free();

        freeSwapchainImages();

        swapChainExtent.free();

        vkDestroySwapchainKHR(logicalDevice().handle(), vkSwapchain, null);
    }

    private void freeSwapchainImages() {
        for(int i = 0;i < swapChainImages.length;i++) {
            swapChainImages[i].free();
            swapChainImages[i] = null;
        }
    }

    private void createSwapchain() {

        try(MemoryStack stack = stackPush()) {

            VulkanPhysicalDevice physicalDevice = physicalDevice();
            VulkanLogicalDevice logicalDevice = logicalDevice();
            long surface = surface().handle();

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

            if(vkCreateSwapchainKHR(logicalDevice.handle(), createInfo, null, pSwapChain) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create swap chain");
            }

            vkSwapchain = pSwapChain.get(0);

            swapChainImageFormat = surfaceFormat.format();

            swapChainExtent = VkExtent2D.malloc().set(extent);

            getSwapchainImages(vkSwapchain, imageCount, stack);
        }
    }

    private void getSwapchainImages(long swapchain, IntBuffer imageCount, MemoryStack stack) {

        vkGetSwapchainImagesKHR(logicalDevice().handle(), swapchain, imageCount, null);

        LongBuffer pSwapchainImages = stack.mallocLong(imageCount.get(0));

        vkGetSwapchainImagesKHR(logicalDevice().handle(), swapchain, imageCount, pSwapchainImages);

        if(swapChainImages == null || swapChainImages.length != imageCount.get(0)) {
            swapChainImages = new VulkanSwapchainImage[imageCount.get(0)];
        }

        for(int i = 0;i < pSwapchainImages.capacity();i++) {
            swapChainImages[i] = new VulkanSwapchainImage(pSwapchainImages.get(i), swapChainImageFormat);
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

    protected VulkanRenderPass createSwapchainRenderPass() {

        depthImage = createDepthImage();

        VulkanRenderPass renderPass = new VulkanRenderPass(
                getSwapchainSubpasses(),
                getSwapchainSubpassAttachments(),
                getSwapchainSubpassDependencies());

        createSwapchainFramebuffers(renderPass);

        return renderPass;
    }

    protected void createSwapchainFramebuffers(VulkanRenderPass renderPass) {

        try(MemoryStack stack = stackPush()) {

            LongBuffer framebufferAttachments = stack.mallocLong(2);

            framebufferAttachments.put(DEPTH_ATTACHMENT_INDEX, depthImage.vkImageView());

            renderPass.createFramebuffers(
                    swapChainExtent.width(),
                    swapChainExtent.height(),
                    swapChainImages.length,
                    index -> framebufferAttachments.put(COLOR_ATTACHMENT_INDEX, swapChainImages[index].vkImageView()));
        }
    }

    protected VulkanSubPassAttachments getSwapchainSubpassAttachments() {

        VkAttachmentDescription.Buffer colorAttachment = VkAttachmentDescription.create(1)
            .format(swapChainImageFormat)
            .samples(VK_SAMPLE_COUNT_1_BIT)
            .loadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
            .storeOp(VK_ATTACHMENT_STORE_OP_STORE)
            .stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
            .stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
            .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
            .finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

        VkAttachmentDescription depthAttachment = VkAttachmentDescription.create()
            .format(findDepthFormat(physicalDevice().handle()))
            .samples(VK_SAMPLE_COUNT_1_BIT)
            .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
            .storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
            .stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
            .stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
            .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
            .finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

        return new VulkanSubPassAttachments(colorAttachment, null, depthAttachment);
    }

    protected VkSubpassDescription.Buffer getSwapchainSubpasses() {
        return VkSubpassDescription.create(1)
                .pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
                .colorAttachmentCount(1)
                .pColorAttachments(getColorAttachmentReference())
                .pDepthStencilAttachment(getDepthAttachmentReference());
    }

    protected VkAttachmentReference.Buffer getColorAttachmentReference() {
        return VkAttachmentReference.create(1)
            .attachment(COLOR_ATTACHMENT_INDEX)
            .layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
    }

    protected VkAttachmentReference getDepthAttachmentReference() {
        return VkAttachmentReference.create()
            .attachment(DEPTH_ATTACHMENT_INDEX)
            .layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
    }

    protected VkSubpassDependency.Buffer getSwapchainSubpassDependencies() {
        return VkSubpassDependency.create(1)
            .srcSubpass(VK_SUBPASS_EXTERNAL)
            .dstSubpass(0)
            .srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
            .srcAccessMask(0)
            .dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
            .dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
    }

    private VulkanImageBase createDepthImage() {

        final int depthFormat = findDepthFormat(physicalDevice().handle());

        VulkanImage depthImage = new VulkanImage(
                getDepthImageInfo(depthFormat),
                getDepthImageViewInfo(depthFormat),
                getDepthImageMemoryProperties());

        depthImage.transitionLayout(VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

        return depthImage;
    }

    private VkImageCreateInfo getDepthImageInfo(int depthFormat) {

        VkImageCreateInfo depthImageViewInfo = VkImageCreateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
            .imageType(VK_IMAGE_TYPE_2D)
            .mipLevels(1)
            .arrayLayers(1)
            .format(depthFormat)
            .tiling(VK_IMAGE_TILING_OPTIMAL)
            .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
            .usage(VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT)
            .samples(VK_SAMPLE_COUNT_1_BIT)
            .sharingMode(VK_SHARING_MODE_EXCLUSIVE);

        depthImageViewInfo.extent()
                .width(swapChainExtent.width())
                .height(swapChainExtent.height())
                .depth(1);

        return depthImageViewInfo;
    }

    private VkImageViewCreateInfo getDepthImageViewInfo(int depthFormat) {

        VkImageViewCreateInfo depthImageViewInfo = VkImageViewCreateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
            .viewType(VK_IMAGE_VIEW_TYPE_2D)
            .format(depthFormat);

        depthImageViewInfo.subresourceRange()
                .aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT)
                .baseMipLevel(0)
                .levelCount(1)
                .baseArrayLayer(0)
                .layerCount(1);

        return depthImageViewInfo;
    }

    private int getDepthImageMemoryProperties() {
        return VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
    }

}
