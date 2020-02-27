package naitsirc98.beryl.graphics.vulkan.vertex;

import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

public class VulkanVertexData extends VertexData {

    protected VulkanVertexData(VertexLayout layout, ByteBuffer[] vertices, ByteBuffer indices) {
        super(layout);
    }

    @Override
    public void bind() { }

    public void bind(VkCommandBuffer commandBuffer) {

    }

    @Override
    public void free() {

    }
}
