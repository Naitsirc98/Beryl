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

import static java.util.Objects.requireNonNull;
import static org.lwjgl.BufferUtils.createIntBuffer;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;

public abstract class VulkanBuffer implements VulkanObject.Long, VmaAllocated, GraphicsBuffer {

    protected long buffer;
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
        return buffer;
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

            allocator().destroyBuffer(handle(), allocation());

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
    public void free() {

        allocator().destroyBuffer(handle(), allocation());
        bufferInfo.free();
        allocationCreateInfo.free();

        buffer = VK_NULL_HANDLE;
        allocation = VK_NULL_HANDLE;
        bufferInfo = null;
        allocationCreateInfo = null;
    }

    protected void init(VmaBufferAllocation bufferAllocation) {
        this.buffer = bufferAllocation.buffer();
        this.allocation = bufferAllocation.allocation();
        this.bufferInfo = bufferAllocation.bufferCreateInfo();
        this.allocationCreateInfo = bufferAllocation.allocationCreateInfo();
    }
}
