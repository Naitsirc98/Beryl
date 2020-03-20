package naitsirc98.beryl.graphics.vulkan.buffers;

import naitsirc98.beryl.graphics.vulkan.textures.VulkanImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static naitsirc98.beryl.util.Asserts.assertTrue;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_CPU_ONLY;
import static org.lwjgl.vulkan.VK11.*;

public class VulkanStagingBuffer extends VulkanCPUBuffer {

    VulkanStagingBuffer() {
        super(getStagingBufferCreateInfo(), getStagingBufferAllocationCreateInfo());
    }

    VulkanStagingBuffer(ByteBuffer data) {
        this();
        allocate(data.remaining());
        update(0, data);
    }

    VulkanStagingBuffer(IntBuffer data) {
        this();
        allocate(data.remaining());
        update(0, data);
    }

    VulkanStagingBuffer(FloatBuffer data) {
        this();
        allocate(data.remaining());
        update(0, data);
    }

    public void transfer(int mipLevel, int xOffset, int yOffset, int zOffset, VulkanImage image) {
        graphicsCommandPool().execute(commandBuffer -> transfer(commandBuffer, mipLevel, xOffset, yOffset, zOffset, image));
    }

    private void transfer(VkCommandBuffer commandBuffer, int mipLevel, int xOffset, int yOffset, int zOffset, VulkanImage image) {

        try(MemoryStack stack = stackPush()) {

            VkBufferImageCopy.Buffer region = VkBufferImageCopy.callocStack(1, stack);
            region.bufferOffset(0);
            region.bufferRowLength(0);   // Tightly packed
            region.bufferImageHeight(0);  // Tightly packed
            region.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            region.imageSubresource().mipLevel(mipLevel);
            region.imageSubresource().baseArrayLayer(0);
            region.imageSubresource().layerCount(1);
            region.imageOffset().set(xOffset, yOffset, zOffset);
            region.imageExtent(VkExtent3D.callocStack(stack).set(image.width(), image.height(), image.depth()));

            vkCmdCopyBufferToImage(commandBuffer, handle(), image.handle(), VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, region);
        }
    }

    public void transfer(long offset, VulkanBuffer buffer) {
        graphicsCommandPool().execute(commandBuffer -> transfer(commandBuffer, offset, buffer));
    }

    private void transfer(VkCommandBuffer commandBuffer, long offset, VulkanBuffer buffer) {

        try(MemoryStack stack = stackPush()) {

            assertTrue((buffer.usage() & VK_BUFFER_USAGE_TRANSFER_DST_BIT) == VK_BUFFER_USAGE_TRANSFER_DST_BIT);

            VkBufferCopy.Buffer copyRegion = VkBufferCopy.callocStack(1, stack)
                    .srcOffset(0)
                    .dstOffset(offset)
                    .size(buffer.size());

            vkCmdCopyBuffer(commandBuffer, handle(), buffer.handle(), copyRegion);
        }
    }

    private static VmaAllocationCreateInfo getStagingBufferAllocationCreateInfo() {
        return VmaAllocationCreateInfo.malloc()
                .usage(VMA_MEMORY_USAGE_CPU_ONLY);
    }

    private static VkBufferCreateInfo getStagingBufferCreateInfo() {
        return VkBufferCreateInfo.malloc()
                .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                .usage(VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
                .sharingMode(VK_SHARING_MODE_EXCLUSIVE);
    }
}
