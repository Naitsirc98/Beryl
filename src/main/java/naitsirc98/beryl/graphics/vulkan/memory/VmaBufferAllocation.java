package naitsirc98.beryl.graphics.vulkan.memory;

import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkBufferCreateInfo;

public final class VmaBufferAllocation extends VmaAllocation {

    private final VkBufferCreateInfo bufferCreateInfo;

    public VmaBufferAllocation(long vmaAllocation, VmaAllocationCreateInfo allocationCreateInfo,
                               long vkBuffer, VkBufferCreateInfo bufferCreateInfo) {
        super(vmaAllocation, allocationCreateInfo, vkBuffer);
        this.bufferCreateInfo = bufferCreateInfo;
    }

    public long buffer() {
        return resource();
    }

    public VkBufferCreateInfo bufferCreateInfo() {
        return bufferCreateInfo;
    }

}
