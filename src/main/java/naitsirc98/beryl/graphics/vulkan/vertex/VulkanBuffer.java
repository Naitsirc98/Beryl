package naitsirc98.beryl.graphics.vulkan.vertex;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.graphics.vulkan.memory.VmaAllocated;
import naitsirc98.beryl.graphics.vulkan.memory.VmaBufferAllocation;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkBufferCreateInfo;

import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;

public class VulkanBuffer implements VulkanObject.Long, VmaAllocated {

    protected long vkBuffer;
    protected long vmaAllocation;
    protected VkBufferCreateInfo bufferCreateInfo;
    private VmaAllocationCreateInfo allocationCreateInfo;

    public VulkanBuffer() {

    }

    public VulkanBuffer(VmaBufferAllocation bufferAllocation) {
        init(bufferAllocation);
    }

    @Override
    public final long handle() {
        return vkBuffer;
    }

    public VkBufferCreateInfo bufferCreateInfo() {
        return bufferCreateInfo;
    }

    @Override
    public long allocation() {
        return vmaAllocation;
    }

    @Override
    public VmaAllocationCreateInfo allocationCreateInfo() {
        return allocationCreateInfo;
    }

    @Override
    public void ensure() {

        if(!available()) {

            allocator().destroyBuffer(handle(), allocation());

            init(allocator().createBuffer(bufferCreateInfo, allocationCreateInfo));
        }
    }

    @Override
    public void free() {

        allocator().destroyBuffer(handle(), allocation());
        bufferCreateInfo.free();

        vkBuffer = VK_NULL_HANDLE;
        vmaAllocation = VK_NULL_HANDLE;
        bufferCreateInfo = null;
    }

    private void init(VmaBufferAllocation bufferAllocation) {
        this.vkBuffer = bufferAllocation.buffer();
        this.vmaAllocation = bufferAllocation.allocation();
        this.bufferCreateInfo = bufferAllocation.bufferCreateInfo();
        this.allocationCreateInfo = bufferAllocation.allocationCreateInfo();
    }
}
