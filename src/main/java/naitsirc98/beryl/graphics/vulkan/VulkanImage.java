package naitsirc98.beryl.graphics.vulkan;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanDevice;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanLogicalDevice;
import naitsirc98.beryl.logging.Log;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanFormatUtils.formatHasStencilComponent;
import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanImage implements VulkanImageBase {

    private final long vkImage;
    private final long vkImageMemory;
    private final long vkImageView;
    private final VkImageCreateInfo imageInfo;
    private final VkImageViewCreateInfo imageViewInfo;
    private final VulkanDevice device;

    public VulkanImage(VulkanDevice device, VkImageCreateInfo imageInfo, VkImageViewCreateInfo imageViewInfo, int memoryProperties) {
        this.device = device;
        this.imageInfo = imageInfo;
        this.imageViewInfo = imageViewInfo;
        vkImage = createVkImage();
        vkImageMemory = createVkImageMemory(memoryProperties);
        bindImageMemory();
        vkImageView = createVkImageView();
    }

    @Override
    public long vkImage() {
        return vkImage;
    }

    @Override
    public long vkImageView() {
        return vkImageView;
    }

    public long getVkImageMemory() {
        return vkImageMemory;
    }

    @Override
    public VulkanLogicalDevice logicalDevice() {
        return device.logicalDevice();
    }

    @Override
    public int format() {
        return imageInfo.format();
    }

    public int mipLevels() {
        return imageInfo.mipLevels();
    }

    @Override
    public void free() {

        imageViewInfo.free();

        imageInfo.free();

        vkDestroyImageView(device.logicalDevice().vkDevice(), vkImageView, null);

        vkDestroyImage(device.logicalDevice().vkDevice(), vkImage, null);

        vkFreeMemory(device.logicalDevice().vkDevice(), vkImageMemory, null);
    }

    public void transitionLayout(int oldLayout, int newLayout) {

        try(MemoryStack stack = stackPush()) {

            IntBuffer stages = stack.mallocInt(2);

            VkImageMemoryBarrier.Buffer barrier = newImageMemoryBarrier(stack);

            setupImageMemoryBarrier(barrier.get(0), oldLayout, newLayout, stages);

            Graphics.vulkan().graphicsCommandPool()
                    .execute(commandBuffer -> doTransition(barrier, stages.get(0), stages.get(1), commandBuffer));
        }
    }

    private void doTransition(VkImageMemoryBarrier.Buffer barrier, int srcStage, int dstStage, VkCommandBuffer commandBuffer) {
        vkCmdPipelineBarrier(commandBuffer,
                srcStage, dstStage,
                0,
                null,
                null,
                barrier);
    }

    private VkImageMemoryBarrier.Buffer newImageMemoryBarrier(MemoryStack stack) {

        VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.callocStack(1, stack);

        barrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
            .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
            .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
            .image(vkImage);

        barrier.subresourceRange().baseMipLevel(0)
            .levelCount(mipLevels())
            .baseArrayLayer(0)
            .layerCount(1);

        return barrier;
    }

    private void setupImageMemoryBarrier(VkImageMemoryBarrier barrier, int oldLayout, int newLayout, IntBuffer stages) {

        barrier.oldLayout(oldLayout).newLayout(newLayout);

        if(newLayout == VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL) {

            barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT);

            if(formatHasStencilComponent(format())) {
                barrier.subresourceRange().aspectMask(
                        barrier.subresourceRange().aspectMask() | VK_IMAGE_ASPECT_STENCIL_BIT);
            }

        } else {
            barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
        }

        int sourceStage;
        int destinationStage;

        if(oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {

            barrier.srcAccessMask(0);
            barrier.dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);

            sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
            destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;

        } else if(oldLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL && newLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {

            barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
            barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);

            sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
            destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;

        } else if (oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL) {

            barrier.srcAccessMask(0);
            barrier.dstAccessMask(VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);

            sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
            destinationStage = VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT;

        } else if(oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL) {

            barrier.srcAccessMask(0);
            barrier.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

            sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
            destinationStage = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;

        } else {
            throw new IllegalArgumentException("Unsupported layout transition");
        }

        stages.put(0, sourceStage).put(1, destinationStage);
    }


    private long createVkImage() {

        try(MemoryStack stack = stackPush()) {

            LongBuffer pImage = stack.mallocLong(1);

            vkCall(vkCreateImage(device.logicalDevice().vkDevice(), imageInfo, null, pImage));

            return pImage.get(0);
        }
    }

    private long createVkImageMemory(int properties) {

        try(MemoryStack stack = stackPush()) {

            VkMemoryRequirements memRequirements = VkMemoryRequirements.mallocStack(stack);
            vkGetImageMemoryRequirements(device.logicalDevice().vkDevice(), vkImage, memRequirements);

            VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.callocStack(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
            allocInfo.allocationSize(memRequirements.size());
            allocInfo.memoryTypeIndex(findMemoryType(memRequirements.memoryTypeBits(), properties));

            LongBuffer pImageMemory = stack.mallocLong(1);

            vkCall(vkAllocateMemory(device.logicalDevice().vkDevice(), allocInfo, null, pImageMemory));

            return pImageMemory.get(0);
        }
    }

    private void bindImageMemory() {
        vkBindImageMemory(device.logicalDevice().vkDevice(), vkImage, vkImageMemory, 0);
    }

    private int findMemoryType(int typeFilter, int properties) {

        VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.mallocStack();
        vkGetPhysicalDeviceMemoryProperties(device.physicalDevice().vkPhysicalDevice(), memProperties);

        for(int i = 0;i < memProperties.memoryTypeCount();i++) {
            if((typeFilter & (1 << i)) != 0 && (memProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
                return i;
            }
        }

        Log.fatal("Failed to find suitable memory type: " + typeFilter + ", " + properties);

        return -1;
    }

    private long createVkImageView() {

        try(MemoryStack stack = stackPush()) {

            imageViewInfo.image(vkImage);

            LongBuffer pImageView = stack.mallocLong(1);

            vkCall(vkCreateImageView(device.logicalDevice().vkDevice(), imageViewInfo, null, pImageView));

            return pImageView.get(0);
        }
    }
}
