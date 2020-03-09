package naitsirc98.beryl.graphics.vulkan.vertex;

import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkBufferCreateInfo;

import static naitsirc98.beryl.graphics.Graphics.vulkan;
import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_GPU_ONLY;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanBufferFactory {

    private static final int VERTEX_BUFFER_USAGE = VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
    private static final int INDEX_BUFFER_USAGE = VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT;

    public static VulkanBuffer newBuffer(VkBufferCreateInfo bufferCreateInfo, VmaAllocationCreateInfo allocationCreateInfo) {
        return new VulkanBuffer(vulkan().allocator().createBuffer(bufferCreateInfo, allocationCreateInfo));
    }

    public static VulkanBuffer newBuffer(int bufferUsage, int memoryUsage, long size, int sharingMode) {

        VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                .size(size)
                .usage(bufferUsage)
                .sharingMode(sharingMode);

        VmaAllocationCreateInfo allocationCreateInfo = VmaAllocationCreateInfo.calloc()
                .usage(memoryUsage);

        return newBuffer(bufferCreateInfo, allocationCreateInfo);
    }

    public static VulkanBuffer newVertexBuffer(long size, int sharingMode) {
        return newBuffer(
                VERTEX_BUFFER_USAGE,
                VMA_MEMORY_USAGE_GPU_ONLY,
                size,
                sharingMode
                );
    }

    public static VulkanBufferGroup newVertexBuffers(int count, long[] sizes, int sharingMode) {

        VulkanBuffer[] buffers = new VulkanBuffer[count];

        for(int i = 0;i < count;i++) {
            buffers[i] = newVertexBuffer(sizes[i], sharingMode);
        }

        return new VulkanBufferGroup(buffers);
    }

    public static VulkanBuffer newIndexBuffer(long size, int sharingMode) {
        return newBuffer(
                INDEX_BUFFER_USAGE,
                VMA_MEMORY_USAGE_GPU_ONLY,
                size,
                sharingMode);
    }

}
