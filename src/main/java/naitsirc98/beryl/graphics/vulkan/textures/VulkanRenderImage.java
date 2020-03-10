package naitsirc98.beryl.graphics.vulkan.textures;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.util.types.IBuilder;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

import java.nio.IntBuffer;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.graphics.vulkan.util.VulkanFormatUtils.formatHasStencilComponent;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanRenderImage implements VulkanObject {

    private VulkanImage image;
    private VulkanImageView imageView;

    public VulkanRenderImage(VulkanImage image, VulkanImageView imageView) {
        this.image = image;
        this.imageView = imageView;
    }

    public VulkanImage image() {
        return image;
    }

    public VulkanImageView view() {
        return imageView;
    }

    public void transitionLayout(int oldLayout, int newLayout) {

        try(MemoryStack stack = stackPush()) {

            IntBuffer stages = stack.mallocInt(2);

            VkImageMemoryBarrier.Buffer barrier = newImageMemoryBarrier(stack);

            setupImageMemoryBarrier(barrier.get(0), oldLayout, newLayout, stages);

            graphicsCommandPool().execute(commandBuffer -> doTransition(barrier, stages.get(0), stages.get(1), commandBuffer));
        }
    }

    @Override
    public void free() {

        image.free();
        imageView.free();

        image = null;
        imageView = null;
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
                .image(image.handle());

        barrier.subresourceRange()
                .baseMipLevel(0)
                .levelCount(image().mipLevels())
                .baseArrayLayer(0)
                .layerCount(1);

        return barrier;
    }

    private void setupImageMemoryBarrier(VkImageMemoryBarrier barrier, int oldLayout, int newLayout, IntBuffer stages) {

        barrier.oldLayout(oldLayout).newLayout(newLayout);

        if(newLayout == VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL) {

            barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT);

            if(formatHasStencilComponent(image.format())) {
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


    public static final class Builder implements IBuilder<VulkanRenderImage> {

        private VkImageCreateInfo imageCreateInfo;
        private VmaAllocationCreateInfo allocationCreateInfo;
        private VkImageViewCreateInfo imageViewCreateInfo;

        public Builder() {
        }

        public Builder imageCreateInfo(VkImageCreateInfo imageCreateInfo) {
            this.imageCreateInfo = requireNonNull(imageCreateInfo);
            return this;
        }

        public Builder allocationCreateInfo(VmaAllocationCreateInfo allocationCreateInfo) {
            this.allocationCreateInfo = requireNonNull(allocationCreateInfo);
            return this;
        }

        public Builder imageViewCreateInfo(VkImageViewCreateInfo imageViewCreateInfo) {
            this.imageViewCreateInfo = requireNonNull(imageViewCreateInfo);
            return this;
        }

        @Override
        public VulkanRenderImage build() {

            VulkanImage image = new VulkanImage(requireNonNull(imageCreateInfo), requireNonNull(allocationCreateInfo));

            VulkanImageView imageView = new VulkanImageView(requireNonNull(imageViewCreateInfo.image(image.handle())));

            return new VulkanRenderImage(image, imageView);
        }
    }
}
