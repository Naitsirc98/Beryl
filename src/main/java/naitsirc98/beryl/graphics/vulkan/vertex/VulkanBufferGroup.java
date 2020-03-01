package naitsirc98.beryl.graphics.vulkan.vertex;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryRequirements;

import java.nio.LongBuffer;
import java.util.stream.LongStream;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanMemoryUtils.allocateMemory;
import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanBufferGroup implements VulkanObject.Custom<LongBuffer> {

    private LongBuffer vkBuffers;
    private LongBuffer memoryOffsets;
    private final int bufferType;
    private long vkMemory;

    public VulkanBufferGroup(int bufferType, int desiredMemoryProperties, long[] bufferSizes) {
        this.bufferType = bufferType;
        vkBuffers = createVkBuffers(bufferSizes);
        memoryOffsets = getMemoryOffsets(bufferSizes);
        vkMemory = allocateMemory(getTotalSize(bufferSizes), getMemoryTypeBits(), desiredMemoryProperties);
        bindBuffersToMemory();
    }

    @Override
    public LongBuffer handle() {
        return vkBuffers;
    }

    public LongBuffer memoryOffsets() {
        return memoryOffsets;
    }

    public int bufferType() {
        return bufferType;
    }

    public long vkMemory() {
        return vkMemory;
    }

    @Override
    public void free() {

        final VkDevice device = logicalDevice().handle();

        for(int i = 0;i < vkBuffers.limit();i++) {
            vkDestroyBuffer(device, vkBuffers.get(i), null);
        }

        memFree(vkBuffers);
        vkBuffers = null;

        memFree(memoryOffsets);

        vkFreeMemory(device, vkMemory, null);
        vkMemory = VK_NULL_HANDLE;
    }

    private void bindBuffersToMemory() {

        final VkDevice device = logicalDevice().handle();

        for(int i = 0;i < vkBuffers.limit();i++) {
            vkBindBufferMemory(device, vkBuffers.get(i), vkMemory, memoryOffsets.get(i));
        }
    }

    private LongBuffer createVkBuffers(long[] bufferSizes) {

        try(MemoryStack stack = stackPush()) {

            LongBuffer vkBuffers = memAllocLong(bufferSizes.length);

            VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                    .usage(bufferType)
                    .sharingMode(VK_SHARING_MODE_EXCLUSIVE);

            final VkDevice device = logicalDevice().handle();

            for(int i = 0;i < bufferSizes.length;i++) {
                vkBuffers.position(i);
                bufferInfo.size(bufferSizes[i]);
                vkCall(vkCreateBuffer(device, bufferInfo, null, vkBuffers));
            }

            return vkBuffers.rewind();
        }
    }

    private LongBuffer getMemoryOffsets(long[] bufferSizes) {

        LongBuffer memoryOffsets = memAllocLong(bufferSizes.length);

        long offset = 0;

        for(int i = 0;i < bufferSizes.length;i++) {
            memoryOffsets.put(i, offset);
            offset += bufferSizes[i];
        }

        return memoryOffsets;
    }

    private int getMemoryTypeBits() {
        try(MemoryStack stack = stackPush()) {
            VkMemoryRequirements memoryRequirements = VkMemoryRequirements.callocStack(stack);
            vkGetBufferMemoryRequirements(logicalDevice().handle(), vkBuffers.get(0), memoryRequirements);
            return memoryRequirements.memoryTypeBits();
        }
    }

    private long getTotalSize(long[] bufferSizes) {
        return LongStream.of(bufferSizes).sum();
    }
}
