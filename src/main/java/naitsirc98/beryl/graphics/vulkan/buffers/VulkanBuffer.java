package naitsirc98.beryl.graphics.vulkan.buffers;

import naitsirc98.beryl.graphics.buffers.GraphicsBuffer;
import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.graphics.vulkan.memory.VmaAllocated;
import naitsirc98.beryl.graphics.vulkan.memory.VmaBufferAllocation;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkBufferCreateInfo;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkToBufferType;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;

public abstract class VulkanBuffer implements VulkanObject.Long, VmaAllocated, GraphicsBuffer {

    protected long vkBuffer;
    protected long allocation;
    protected VkBufferCreateInfo bufferCreateInfo;
    protected VmaAllocationCreateInfo allocationCreateInfo;

    public VulkanBuffer(VkBufferCreateInfo bufferCreateInfo, VmaAllocationCreateInfo allocationCreateInfo) {
        this.bufferCreateInfo = bufferCreateInfo;
        this.allocationCreateInfo = allocationCreateInfo;
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
        return allocation;
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
    public Type type() {
        return vkToBufferType(bufferCreateInfo.usage());
    }

    @Override
    public void allocate(long bytes) {
        bufferCreateInfo.size(bytes);
        init(allocator().createBuffer(bufferCreateInfo, allocationCreateInfo));
    }

    @Override
    public void data(ByteBuffer data) {
        if(bufferCreateInfo.size() != data.remaining()) {
            allocate(data.remaining());
        }
        update(0, data);
    }

    @Override
    public void data(IntBuffer data) {
        if(bufferCreateInfo.size() != data.remaining()) {
            allocate(data.remaining());
        }
        update(0, data);
    }

    @Override
    public void data(FloatBuffer data) {
        if(bufferCreateInfo.size() != data.remaining()) {
            allocate(data.remaining());
        }
        update(0, data);
    }

    @Override
    public void free() {

        allocator().destroyBuffer(handle(), allocation());
        bufferCreateInfo.free();
        allocationCreateInfo.free();

        vkBuffer = VK_NULL_HANDLE;
        allocation = VK_NULL_HANDLE;
        bufferCreateInfo = null;
        allocationCreateInfo = null;
    }

    protected void init(VmaBufferAllocation bufferAllocation) {
        this.vkBuffer = bufferAllocation.buffer();
        this.allocation = bufferAllocation.allocation();
        this.bufferCreateInfo = bufferAllocation.bufferCreateInfo();
        this.allocationCreateInfo = bufferAllocation.allocationCreateInfo();
    }
}
