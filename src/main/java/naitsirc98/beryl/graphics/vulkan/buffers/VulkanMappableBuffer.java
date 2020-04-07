package naitsirc98.beryl.graphics.vulkan.buffers;

import naitsirc98.beryl.graphics.buffers.GraphicsMappableBuffer;
import naitsirc98.beryl.graphics.vulkan.memory.VmaBufferAllocation;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkBufferCreateInfo;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static org.lwjgl.system.libc.LibCString.memcpy;
import static org.lwjgl.system.libc.LibCString.nmemcpy;
import static org.lwjgl.util.vma.Vma.vmaMapMemory;
import static org.lwjgl.util.vma.Vma.vmaUnmapMemory;

public abstract class VulkanMappableBuffer extends VulkanBuffer implements GraphicsMappableBuffer {

    public static void copy(VulkanMappableBuffer src, VulkanMappableBuffer dst, long size) {
        assertTrue(src.size() >= size);

        if(dst.size() < size) {
            dst.allocate(size);
        }

        final long srcMemory = src.mapMemory(0).get(0);

        final long dstMemory = dst.mapMemory(0).get(0);

        nmemcpy(dstMemory, srcMemory, size);

        dst.unmapMemory();

        src.unmapMemory();
    }

    public VulkanMappableBuffer(VkBufferCreateInfo bufferCreateInfo, VmaAllocationCreateInfo allocationCreateInfo) {
        super(bufferCreateInfo, allocationCreateInfo);
    }

    public VulkanMappableBuffer(VmaBufferAllocation bufferAllocation) {
        super(bufferAllocation);
    }

    @Override
    public void update(long offset, ByteBuffer data) {

        PointerBuffer pMemoryData = mapMemory(offset);

        memcpy(pMemoryData.getByteBuffer(0, data.remaining()), data);

        unmapMemory();
    }

    @Override
    public void update(long offset, IntBuffer data) {

        PointerBuffer pMemoryData = mapMemory(offset);

        memcpy(pMemoryData.getIntBuffer(0, data.remaining()), data);

        unmapMemory();
    }

    @Override
    public void update(long offset, FloatBuffer data) {

        PointerBuffer pMemoryData = mapMemory(offset);

        memcpy(pMemoryData.getFloatBuffer(0, data.remaining()), data);

        unmapMemory();
    }

    @Override
    public PointerBuffer mapMemory(long offset) {

        PointerBuffer pMemoryData = PointerBuffer.allocateDirect(1);

        vkCall(vmaMapMemory(allocator().handle(), allocation, pMemoryData));

        pMemoryData.put(pMemoryData.get(0) + offset);

        return pMemoryData;
    }

    @Override
    public void unmapMemory() {
        vmaUnmapMemory(allocator().handle(), allocation);
    }
}
