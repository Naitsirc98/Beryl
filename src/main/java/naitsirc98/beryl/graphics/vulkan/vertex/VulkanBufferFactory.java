package naitsirc98.beryl.graphics.vulkan.vertex;

import static org.lwjgl.vulkan.VK10.*;

public class VulkanBufferFactory {

    public static VulkanBuffer newVertexBuffer(int size) {
        return new VulkanBuffer(size,
                VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                VK_MEMORY_HEAP_DEVICE_LOCAL_BIT);
    }

    public static VulkanBuffer newIndexBuffer(int size) {
        return new VulkanBuffer(size,
                VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                VK_MEMORY_HEAP_DEVICE_LOCAL_BIT);
    }

}
