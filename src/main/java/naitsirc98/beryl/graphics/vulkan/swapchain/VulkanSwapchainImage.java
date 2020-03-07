package naitsirc98.beryl.graphics.vulkan.swapchain;

import naitsirc98.beryl.graphics.vulkan.VulkanImageBase;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public final class VulkanSwapchainImage implements VulkanImageBase {

    private static final int MIP_LEVELS = 1;

    private final long vkImage;
    private long vkImageView;
    private final int imageFormat;

    public VulkanSwapchainImage(long vkImage, int imageFormat) {
        this.vkImage = vkImage;
        this.imageFormat = imageFormat;
        this.vkImageView = createSwapchainImageView();
    }

    @Override
    public long handle() {
        return vkImage;
    }

    @Override
    public long vkImageView() {
        return vkImageView;
    }

    @Override
    public int format() {
        return imageFormat;
    }

    @Override
    public void free() {
        // VkImage is automatically destroyed by the swapchain
        vkDestroyImageView(logicalDevice().handle(), vkImageView, null);
        vkImageView = VK_NULL_HANDLE;
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

            if(vkCreateImageView(logicalDevice().handle(), viewInfo, null, pImageView) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create texture image view");
            }

            return pImageView.get(0);
        }
    }
}
