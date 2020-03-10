package naitsirc98.beryl.graphics.vulkan.buffers;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.libc.LibCString.nmemcpy;
import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.VK11.*;

public class VulkanStagingBuffer extends VulkanBuffer {

    public VulkanStagingBuffer() {
        super(getStagingBufferCreateInfo(), getStagingBufferAllocationCreateInfo());
    }

    public VulkanStagingBuffer(ByteBuffer data) {
        this();
        allocate(data.remaining());
        update(0, data);
    }

    public VulkanStagingBuffer(IntBuffer data) {
        this();
        allocate(data.remaining());
        update(0, data);
    }

    public VulkanStagingBuffer(FloatBuffer data) {
        this();
        allocate(data.remaining());
        update(0, data);
    }

    public void transfer(long offset, VulkanBuffer buffer) {
        graphicsCommandPool().execute(commandBuffer -> transfer(commandBuffer, offset, buffer));
    }

    private void transfer(VkCommandBuffer commandBuffer, long offset, VulkanBuffer buffer) {

        try(MemoryStack stack = stackPush()) {

            assertTrue((buffer.usage() & VK_BUFFER_USAGE_TRANSFER_DST_BIT) == VK_BUFFER_USAGE_TRANSFER_DST_BIT);

            VkBufferCopy.Buffer copyRegion = VkBufferCopy.callocStack(1, stack)
                    .srcOffset(0)
                    .dstOffset(offset)
                    .size(buffer.size());

            vkCmdCopyBuffer(commandBuffer, handle(), buffer.handle(), copyRegion);
        }
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


    private static VmaAllocationCreateInfo getStagingBufferAllocationCreateInfo() {
        return VmaAllocationCreateInfo.malloc()
                .usage(VMA_MEMORY_USAGE_CPU_ONLY);
    }

    private static VkBufferCreateInfo getStagingBufferCreateInfo() {
        return VkBufferCreateInfo.malloc()
                .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                .usage(VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
                .sharingMode(VK_SHARING_MODE_EXCLUSIVE);
    }
}
