package naitsirc98.beryl.graphics.vulkan.textures;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.graphics.vulkan.memory.VmaAllocated;
import naitsirc98.beryl.graphics.vulkan.memory.VmaImageAllocation;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static naitsirc98.beryl.graphics.vulkan.buffers.VulkanBufferUtils.transferToImage;
import static naitsirc98.beryl.graphics.vulkan.util.VulkanFormatUtils.formatHasStencilComponent;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanImage implements VmaAllocated, VulkanObject.Long {

    private long vkImage;
    private long allocation;
    private VkImageCreateInfo imageInfo;
    private VmaAllocationCreateInfo allocationCreateInfo;
    private int layout;

    public VulkanImage() {
    }

    public VulkanImage(VkImageCreateInfo imageInfo, VmaAllocationCreateInfo allocationInfo) {
        init(allocator().createImage(imageInfo, allocationInfo));
    }

    public void init(VmaImageAllocation imageAllocation) {
        this.vkImage = imageAllocation.image();
        this.allocation = imageAllocation.allocation();
        this.imageInfo = imageAllocation.imageCreateInfo();
        this.allocationCreateInfo = imageAllocation.allocationCreateInfo();
        this.layout = imageInfo.initialLayout();
    }

    @Override
    public long handle() {
        return vkImage;
    }

    @Override
    public long allocation() {
        return allocation;
    }

    public void resize(int width, int height, int depth) {

        if(width() == width && height() == height && depth() == depth) {
            return;
        }

        allocator().destroyImage(vkImage, allocation);

        imageInfo.extent().set(width, height, depth);

        init(allocator().createImage(imageInfo, allocationCreateInfo));
    }

    public void pixels(int mipLevel, int xOffset, int yOffset, int zOffset, ByteBuffer pixels) {
        if(layout != VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {
            transitionLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
        }
        transferToImage(mipLevel, xOffset, yOffset, zOffset, pixels, this);
    }

    public void pixels(int mipLevel, int xOffset, int yOffset, int zOffset, FloatBuffer pixels) {
        if(layout != VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {
            transitionLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
        }
        transferToImage(mipLevel, xOffset, yOffset, zOffset, pixels, this);
    }

    public int format() {
        return imageInfo.format();
    }

    public int tiling() {
        return imageInfo.tiling();
    }

    public int width() {
        return imageInfo.extent().width();
    }

    public int height() {
        return imageInfo.extent().height();
    }

    public int depth() {
        return imageInfo.extent().depth();
    }

    public int mipLevels() {
        return imageInfo.mipLevels();
    }

    public int arrayLayers() {
        return imageInfo.arrayLayers();
    }

    public int type() {
        return imageInfo.imageType();
    }

    public int initialLayout() {
        return imageInfo.initialLayout();
    }

    public int layout() {
        return layout;
    }

    public int samples() {
        return imageInfo.samples();
    }

    public int sharingMode() {
        return imageInfo.sharingMode();
    }

    public int flags() {
        return imageInfo.flags();
    }

    @Override
    public VmaAllocationCreateInfo allocationCreateInfo() {
        return allocationCreateInfo;
    }

    @Override
    public void ensure() {
        // TODO
    }

    public void transitionLayout(int newLayout) {

        try(MemoryStack stack = stackPush()) {

            IntBuffer stages = stack.mallocInt(2);

            VkImageMemoryBarrier.Buffer barrier = newImageMemoryBarrier(stack);

            setupImageMemoryBarrier(barrier.get(0), layout, newLayout, stages);

            graphicsCommandPool().execute(commandBuffer -> doTransition(barrier, stages.get(0), stages.get(1), commandBuffer));

            layout = newLayout;
        }
    }

    @Override
    public void free() {

        if(vkImage == VK_NULL_HANDLE) {
            return;
        }

        allocator().destroyImage(vkImage, allocation);
        imageInfo.free();
        allocationCreateInfo.free();


        vkImage = VK_NULL_HANDLE;
        allocation = VK_NULL_HANDLE;
        imageInfo = null;
        allocationCreateInfo = null;
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
                .image(handle());

        barrier.subresourceRange()
                .baseMipLevel(0)
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
}
