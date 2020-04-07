package naitsirc98.beryl.graphics.vulkan.buffers;

import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkBufferCreateInfo;

import static org.lwjgl.util.vma.Vma.VMA_ALLOCATION_CREATE_MAPPED_BIT;
import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_CPU_TO_GPU;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanUniformBuffer extends VulkanMappableBuffer {

    public static VulkanUniformBuffer[] create(int count, long size) {

        VulkanUniformBuffer[] uniformBuffers = new VulkanUniformBuffer[count];

        for(int i = 0;i < count;i++) {
            uniformBuffers[i] = new VulkanUniformBuffer(size);
        }

        return uniformBuffers;
    }

    public VulkanUniformBuffer() {
        super(getUniformBufferCreateInfo(0), getUniformBufferAllocationCreateInfo());
        // Need to call allocate(long size) afterwards
    }

    public VulkanUniformBuffer(long size) {
        super(getUniformBufferCreateInfo(size), getUniformBufferAllocationCreateInfo());
        init(allocator().createBuffer(bufferInfo, allocationCreateInfo));
    }

    private static VmaAllocationCreateInfo getUniformBufferAllocationCreateInfo() {
        return VmaAllocationCreateInfo.malloc()
                .usage(VMA_MEMORY_USAGE_CPU_TO_GPU)
                .flags(VMA_ALLOCATION_CREATE_MAPPED_BIT);
                // .requiredFlags(VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
    }

    private static VkBufferCreateInfo getUniformBufferCreateInfo(long size) {
        return VkBufferCreateInfo.malloc()
                .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                .usage(VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
                .sharingMode(VK_SHARING_MODE_EXCLUSIVE)
                .size(size);
    }
}
