package naitsirc98.beryl.graphics.vulkan.buffers;

import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkBufferCreateInfo;

import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_CPU_TO_GPU;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanUniformBuffer extends VulkanCPUBuffer {

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
                .requiredFlags(VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
    }

    private static VkBufferCreateInfo getUniformBufferCreateInfo(long size) {
        return VkBufferCreateInfo.malloc()
                .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                .usage(VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
                .sharingMode(VK_SHARING_MODE_EXCLUSIVE)
                .size(size);
    }
}
