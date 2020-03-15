package naitsirc98.beryl.graphics.vulkan.buffers;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkBufferCreateInfo;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.libc.LibCString.nmemcpy;
import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanUniformBuffer extends VulkanBuffer {

    public VulkanUniformBuffer() {
        super(getUniformBufferCreateInfo(0), getUniformBufferAllocationCreateInfo());
    }

    public VulkanUniformBuffer(long size) {
        super(getUniformBufferCreateInfo(size), getUniformBufferAllocationCreateInfo());
    }

    @Override
    public void update(long offset, ByteBuffer data) {
        update(offset, memAddress(data), data.remaining());
    }

    @Override
    public void update(long offset, IntBuffer data) {
        update(offset, memAddress(data), data.remaining());
    }

    @Override
    public void update(long offset, FloatBuffer data) {
        update(offset, memAddress(data), data.remaining());
    }

    private void update(long offset, long src, int size) {
        try(MemoryStack stack = stackPush()) {

            PointerBuffer pMemoryData = stack.mallocPointer(1);

            vkCall(vmaMapMemory(allocator().handle(), allocation, pMemoryData));

            nmemcpy(pMemoryData.get(0) + offset, src, size);

            vmaUnmapMemory(allocator().handle(), allocation);
        }
    }

    private static VmaAllocationCreateInfo getUniformBufferAllocationCreateInfo() {
        return VmaAllocationCreateInfo.malloc()
                .usage(VMA_MEMORY_USAGE_CPU_TO_GPU);
    }

    private static VkBufferCreateInfo getUniformBufferCreateInfo(long size) {
        return VkBufferCreateInfo.malloc()
                .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                .usage(VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
                .sharingMode(VK_SHARING_MODE_EXCLUSIVE)
                .size(size);
    }
}
