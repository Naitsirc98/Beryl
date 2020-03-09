package naitsirc98.beryl.graphics.vulkan.buffers;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;

public class VulkanVertexBuffer extends VulkanGPUBuffer {

    public static VulkanVertexBuffer[] create(int count) {

        VulkanVertexBuffer[] buffers = new VulkanVertexBuffer[count];

        for(int i = 0;i < count;i++) {
            buffers[i] = new VulkanVertexBuffer();
        }

        return buffers;
    }

    public VulkanVertexBuffer() {
        super(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
    }
}
