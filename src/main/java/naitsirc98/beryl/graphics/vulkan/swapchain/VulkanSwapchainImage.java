package naitsirc98.beryl.graphics.vulkan.swapchain;

import naitsirc98.beryl.graphics.vulkan.devices.VulkanLogicalDevice;
import naitsirc98.beryl.util.Destructor;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

@Destructor
public final class VulkanSwapchainImage implements NativeResource {

    private static final int MIP_LEVELS = 1;

    private final VulkanLogicalDevice logicalDevice;
    private final long vkImage;
    private final long vkImageView;
    private final int imageFormat;

    public VulkanSwapchainImage(VulkanLogicalDevice logicalDevice, long vkImage, int imageFormat) {
        this.logicalDevice = logicalDevice;
        this.vkImage = vkImage;
        this.imageFormat = imageFormat;
        this.vkImageView = createSwapchainImageView();
    }

    public long vkImage() {
        return vkImage;
    }

    public int imageFormat() {
        return imageFormat;
    }

    public long vkImageView() {
        return vkImageView;
    }

    @Override
    public void free() {
        // VkImage is automatically destroy by the swapchain
        vkDestroyImageView(logicalDevice.vkDevice(), vkImageView, null);
    }

    private long createSwapchainImageView() {

        try(MemoryStack stack = stackPush()) {

            VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.callocStack(stack);
            viewInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
            viewInfo.image(vkImage);
            viewInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
            viewInfo.format(imageFormat);
            viewInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            viewInfo.subresourceRange().baseMipLevel(0);
            viewInfo.subresourceRange().levelCount(MIP_LEVELS);
            viewInfo.subresourceRange().baseArrayLayer(0);
            viewInfo.subresourceRange().layerCount(1);

            LongBuffer pImageView = stack.mallocLong(1);

            if(vkCreateImageView(logicalDevice.vkDevice(), viewInfo, null, pImageView) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create texture image view");
            }

            return pImageView.get(0);
        }
    }
}
