package naitsirc98.beryl.graphics.vulkan.buffers;

import naitsirc98.beryl.graphics.vulkan.memory.VmaBufferAllocation;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkBufferCreateInfo;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.libc.LibCString.memcpy;
import static org.lwjgl.util.vma.Vma.vmaMapMemory;
import static org.lwjgl.util.vma.Vma.vmaUnmapMemory;

public abstract class VulkanCPUBuffer extends VulkanBuffer {

    public VulkanCPUBuffer(VkBufferCreateInfo bufferCreateInfo, VmaAllocationCreateInfo allocationCreateInfo) {
        super(bufferCreateInfo, allocationCreateInfo);
    }

    public VulkanCPUBuffer(VmaBufferAllocation bufferAllocation) {
        super(bufferAllocation);
    }

    @Override
    public void update(long offset, ByteBuffer data) {
        try(MemoryStack stack = stackPush()) {

            PointerBuffer pMemoryData = stack.mallocPointer(1);

            vkCall(vmaMapMemory(allocator().handle(), allocation, pMemoryData));

            memcpy(pMemoryData.getByteBuffer(0, data.remaining()).position((int) offset), data);

            vmaUnmapMemory(allocator().handle(), allocation);
        }
    }

    @Override
    public void update(long offset, IntBuffer data) {
        try(MemoryStack stack = stackPush()) {

            PointerBuffer pMemoryData = stack.mallocPointer(1);

            vkCall(vmaMapMemory(allocator().handle(), allocation, pMemoryData));

            memcpy(pMemoryData.getIntBuffer(0, data.remaining()).position((int) offset), data);

            vmaUnmapMemory(allocator().handle(), allocation);
        }
    }

    @Override
    public void update(long offset, FloatBuffer data) {
        try(MemoryStack stack = stackPush()) {

            PointerBuffer pMemoryData = stack.mallocPointer(1);

            vkCall(vmaMapMemory(allocator().handle(), allocation, pMemoryData));

            memcpy(pMemoryData.getFloatBuffer(0, data.remaining()).position((int) offset), data);

            vmaUnmapMemory(allocator().handle(), allocation);
        }
    }
}
