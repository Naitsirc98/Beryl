package naitsirc98.beryl.graphics.vulkan.buffers;

import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkBufferCreateInfo;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_GPU_ONLY;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;

public abstract class VulkanGPUBuffer extends VulkanBuffer {

    public VulkanGPUBuffer(int usage) {
        super(getVertexBufferCreateInfo(usage), getVertexBufferAllocationCreateInfo());
    }

    @Override
    public void update(long offset, ByteBuffer data) {
        VulkanStagingBuffer.transfer(offset, data, this);
    }

    @Override
    public void update(long offset, IntBuffer data) {
        VulkanStagingBuffer.transfer(offset, data, this);
    }

    @Override
    public void update(long offset, FloatBuffer data) {
        VulkanStagingBuffer.transfer(offset, data, this);
    }

    private static VkBufferCreateInfo getVertexBufferCreateInfo(int usage) {
        return VkBufferCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                .usage(VK_BUFFER_USAGE_TRANSFER_DST_BIT | usage)
                .sharingMode(VK_SHARING_MODE_EXCLUSIVE);
    }

    private static VmaAllocationCreateInfo getVertexBufferAllocationCreateInfo() {
        return VmaAllocationCreateInfo.malloc()
                .usage(VMA_MEMORY_USAGE_GPU_ONLY);
    }
}
