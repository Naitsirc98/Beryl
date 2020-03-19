package naitsirc98.beryl.graphics.vulkan.buffers;

import naitsirc98.beryl.graphics.buffers.GraphicsCPUBuffer;
import naitsirc98.beryl.graphics.vulkan.memory.VmaBufferAllocation;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkBufferCreateInfo;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.libc.LibCString.memcpy;
import static org.lwjgl.system.libc.LibCString.nmemcpy;
import static org.lwjgl.util.vma.Vma.vmaMapMemory;
import static org.lwjgl.util.vma.Vma.vmaUnmapMemory;

public abstract class VulkanCPUBuffer extends VulkanBuffer implements GraphicsCPUBuffer {

    public static void copy(VulkanCPUBuffer src, VulkanCPUBuffer dst, long size) {
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

    public VulkanCPUBuffer(VkBufferCreateInfo bufferCreateInfo, VmaAllocationCreateInfo allocationCreateInfo) {
        super(bufferCreateInfo, allocationCreateInfo);
    }

    public VulkanCPUBuffer(VmaBufferAllocation bufferAllocation) {
        super(bufferAllocation);
    }

    @Override
    public void update(long offset, ByteBuffer data) {
        try(MemoryStack stack = stackPush()) {

            PointerBuffer pMemoryData = mapMemory(offset);

            memcpy(pMemoryData.getByteBuffer(0, data.remaining()), data);

            unmapMemory();
        }
    }

    @Override
    public void update(long offset, IntBuffer data) {
        try(MemoryStack stack = stackPush()) {

            PointerBuffer pMemoryData = mapMemory(offset);

            memcpy(pMemoryData.getIntBuffer(0, data.remaining()), data);

            unmapMemory();
        }
    }

    @Override
    public void update(long offset, FloatBuffer data) {
        try(MemoryStack stack = stackPush()) {

            PointerBuffer pMemoryData = mapMemory(offset);

            memcpy(pMemoryData.getFloatBuffer(0, data.remaining()), data);

            unmapMemory();
        }
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
