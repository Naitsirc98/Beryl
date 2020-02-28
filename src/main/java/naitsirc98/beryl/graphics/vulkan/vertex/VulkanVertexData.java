package naitsirc98.beryl.graphics.vulkan.vertex;

import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.lwjgl.vulkan.VK10.*;

public final class VulkanVertexData extends VertexData {

    private VulkanBufferGroup vertexBuffers;
    private VulkanBuffer indexBuffer;

    protected VulkanVertexData(VertexLayout layout, ByteBuffer[] vertices, ByteBuffer indices) {
        super(layout);
        vertexBuffers = VulkanBufferFactory.newVertexBuffers(getVertexBufferSizes(vertices));
    }

    public void bind(VkCommandBuffer commandBuffer) {

        vkCmdBindVertexBuffers(commandBuffer, 0, vertexBuffers.vkBuffers(), vertexBuffers.memoryOffsets());

        if(indexBuffer != null) {
            vkCmdBindIndexBuffer(commandBuffer, indexBuffer.vkBuffer(), 0, VK_INDEX_TYPE_UINT32);
        }
    }

    @Override
    public void free() {

        vertexBuffers.free();
        vertexBuffers = null;

        if(indexBuffer != null) {
            indexBuffer.free();
            indexBuffer = null;
        }
    }

    private long[] getVertexBufferSizes(ByteBuffer[] vertices) {
        return Arrays.stream(vertices).mapToLong(ByteBuffer::limit).toArray();
    }
}
