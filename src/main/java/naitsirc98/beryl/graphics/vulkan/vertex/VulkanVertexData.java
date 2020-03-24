package naitsirc98.beryl.graphics.vulkan.vertex;

import naitsirc98.beryl.graphics.vulkan.buffers.VulkanBufferGroup;
import naitsirc98.beryl.graphics.vulkan.buffers.VulkanIndexBuffer;
import naitsirc98.beryl.graphics.vulkan.buffers.VulkanVertexBuffer;
import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.*;

public final class VulkanVertexData extends VertexData {

    private VulkanBufferGroup vertexBuffers;
    private VulkanIndexBuffer indexBuffer;

    protected VulkanVertexData(VertexLayout layout, int firstVertex, int vertexCount, ByteBuffer[] vertices, ByteBuffer indices, int indexCount) {
        super(layout, firstVertex, vertexCount, indexCount);
        vertexBuffers = createVertexBuffers(vertices);
        if(indices != null) {
            indexBuffer = new VulkanIndexBuffer();
            indexBuffer.data(indices);
        }
    }

    private VulkanBufferGroup createVertexBuffers(ByteBuffer[] vertices) {

        VulkanVertexBuffer[] vertexBuffers = VulkanVertexBuffer.create(vertices.length);

        setVertexBuffersData(vertexBuffers, vertices);

        return new VulkanBufferGroup(vertexBuffers);
    }

    public void bind(VkCommandBuffer commandBuffer) {

        vkCmdBindVertexBuffers(commandBuffer, 0, vertexBuffers.pBuffers(), vertexBuffers.pOffsets());

        if(indexBuffer != null) {
            vkCmdBindIndexBuffer(commandBuffer, indexBuffer.handle(), 0, VK_INDEX_TYPE_UINT32);
        }
    }

    @Override
    protected void free() {

        vertexBuffers.release();
        vertexBuffers = null;

        if(indexBuffer != null) {
            indexBuffer.release();
            indexBuffer = null;
        }
    }

    private void setVertexBuffersData(VulkanVertexBuffer[] vertexBuffers, ByteBuffer[] vertices) {
        for(int i = 0;i < vertices.length;i++) {
            vertexBuffers[i].data(vertices[i]);
        }
    }

}
