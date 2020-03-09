package naitsirc98.beryl.graphics.vulkan.buffers;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT;

public class VulkanIndexBuffer extends VulkanGPUBuffer {

    public VulkanIndexBuffer() {
        super(VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
    }
}
