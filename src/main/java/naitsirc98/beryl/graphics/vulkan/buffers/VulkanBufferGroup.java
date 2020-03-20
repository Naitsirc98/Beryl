package naitsirc98.beryl.graphics.vulkan.buffers;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;

import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;

public class VulkanBufferGroup implements VulkanObject {

    private VulkanBuffer[] buffers;
    private LongBuffer pBuffers;
    private LongBuffer pOffsets;

    public VulkanBufferGroup() {
    }

    public VulkanBufferGroup(VulkanBuffer[] buffers) {
        init(buffers);
    }

    public VulkanBuffer[] buffers() {
        return buffers;
    }

    public LongBuffer pBuffers() {
        return pBuffers;
    }

    public LongBuffer pOffsets() {
        return pOffsets;
    }

    public void init(VulkanBuffer[] buffers) {

        this.buffers = buffers;

        pBuffers = memAllocLong(buffers.length);
        pOffsets = memAllocLong(buffers.length);

        for(int i = 0;i < buffers.length;i++) {
            pBuffers.put(i, buffers[i].handle());
            pOffsets.put(i, buffers[i].offset());
        }
    }

    public void ensure() {
        // TODO
    }

    @Override
    public void release() {

        for(VulkanBuffer buffer : buffers) {
            buffer.release();
        }
        memFree(pBuffers);
        memFree(pOffsets);

        buffers = null;
        pBuffers = null;
        pOffsets = null;
    }
}
