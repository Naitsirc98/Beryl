package naitsirc98.beryl.graphics.vulkan.buffers;

import naitsirc98.beryl.graphics.buffers.GraphicsBuffer;
import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.graphics.vulkan.memory.VmaAllocated;
import naitsirc98.beryl.graphics.vulkan.memory.VmaBufferAllocation;
import naitsirc98.beryl.resources.ManagedResource;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkBufferCreateInfo;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.BufferUtils.createIntBuffer;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;

public abstract class VulkanBuffer extends ManagedResource implements VulkanObject.Long, VmaAllocated, GraphicsBuffer {

    protected long handle;
    protected long allocation;
    protected VkBufferCreateInfo bufferInfo;
    protected VmaAllocationCreateInfo allocationCreateInfo;

    public VulkanBuffer(VkBufferCreateInfo bufferCreateInfo, VmaAllocationCreateInfo allocationCreateInfo) {
        this.bufferInfo = bufferCreateInfo;
        this.allocationCreateInfo = allocationCreateInfo;
    }

    public VulkanBuffer(VmaBufferAllocation bufferAllocation) {
        init(bufferAllocation);
    }

    @Override
    public final long handle() {
        return handle;
    }

    @Override
    public long allocation() {
        return allocation;
    }

    @Override
    public VmaAllocationCreateInfo allocationCreateInfo() {
        return allocationCreateInfo;
    }

    public long size() {
        return bufferInfo.size();
    }

    public int usage() {
        return bufferInfo.usage();
    }

    public int sharingMode() {
        return bufferInfo.sharingMode();
    }

    public IntBuffer queueFamilyIndices() {
        return bufferInfo.queueFamilyIndexCount() == 0
                ? null
                : createIntBuffer(bufferInfo.queueFamilyIndexCount()).put(requireNonNull(bufferInfo.pQueueFamilyIndices())).rewind();
    }

    public int flags() {
        return bufferInfo.flags();
    }

    @Override
    public void ensure() {

        if(!available()) {

            destroyVkBuffer();

            init(allocator().createBuffer(bufferInfo, allocationCreateInfo));
        }
    }

    @Override
    public Type type() {
        return mapper().mapBufferTypeFromAPI(bufferInfo.usage());
    }

    @Override
    public void allocate(long bytes) {

        bufferInfo.size(bytes);

        if(handle != VK_NULL_HANDLE && allocation != VK_NULL_HANDLE) {
            destroyVkBuffer();
        }

        init(allocator().createBuffer(bufferInfo, allocationCreateInfo));
    }

    @Override
    public void data(ByteBuffer data) {
        if(bufferInfo.size() != data.remaining()) {
            allocate(data.remaining());
        }
        update(0, data);
    }

    @Override
    public void data(IntBuffer data) {
        if(bufferInfo.size() != data.remaining()) {
            allocate(data.remaining());
        }
        update(0, data);
    }

    @Override
    public void data(FloatBuffer data) {
        if(bufferInfo.size() != data.remaining()) {
            allocate(data.remaining());
        }
        update(0, data);
    }

    @Override
    public boolean released() {
        return false;
    }

    @Override
    protected void free() {

        destroyVkBuffer();

        bufferInfo.free();
        allocationCreateInfo.free();

        bufferInfo = null;
        allocationCreateInfo = null;
    }

    private void destroyVkBuffer() {
        allocator().destroyBuffer(handle(), allocation());
        handle = VK_NULL_HANDLE;
        allocation = VK_NULL_HANDLE;
    }

    protected void init(VmaBufferAllocation bufferAllocation) {
        this.handle = bufferAllocation.buffer();
        this.allocation = bufferAllocation.allocation();
        this.bufferInfo = bufferAllocation.bufferCreateInfo();
        this.allocationCreateInfo = bufferAllocation.allocationCreateInfo();
    }

    @Override
    public String toString() {
        return "VulkanBuffer{" +
                "handle=" + handle +
                ", allocation=" + allocation +
                ", bufferInfo=" + bufferInfo +
                ", allocationCreateInfo=" + allocationCreateInfo +
                ", size = " + size() +
                ", offset = " + allocationOffset() +
                '}';
    }
}
