package naitsirc98.beryl.graphics.vulkan.vertex;

import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static naitsirc98.beryl.graphics.vulkan.vertex.VulkanBufferUtils.setVulkanBufferData;
import static org.lwjgl.vulkan.VK10.*;

public final class VulkanVertexData extends VertexData {

    private VulkanBufferGroup vertexBuffers;
    private VulkanBuffer indexBuffer;

    protected VulkanVertexData(VertexLayout layout, int firstVertex, int vertexCount, ByteBuffer[] vertices, ByteBuffer indices) {
        super(layout, firstVertex, vertexCount, indices == null ? 0 : indices.remaining());
        vertexBuffers = VulkanBufferFactory.newVertexBuffers(getVertexBufferSizes(vertices));
        setVertexBuffersData(vertices);
        // TODO: setIndexBufferData(indices);
    }

    public void bind(VkCommandBuffer commandBuffer) {

        vkCmdBindVertexBuffers(commandBuffer, 0, vertexBuffers.handle(), vertexBuffers.memoryOffsets());

        if(indexBuffer != null) {
            vkCmdBindIndexBuffer(commandBuffer, indexBuffer.handle(), 0, VK_INDEX_TYPE_UINT32);
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

    private void setVertexBuffersData(ByteBuffer[] vertices) {
        for(int i = 0;i < vertices.length;i++) {
            setVulkanBufferData(vertexBuffers.handle().get(i), vertices[i]);
        }
    }
}
