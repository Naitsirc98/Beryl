package naitsirc98.beryl.graphics.vulkan.vertex;

import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;

import java.nio.ByteBuffer;

public class VulkanVertexDataBuilder extends VertexData.Builder {

    private ByteBuffer[] vertices;
    private ByteBuffer indices;

    public VulkanVertexDataBuilder(VertexLayout layout) {
        super(layout);
        vertices = new ByteBuffer[layout.bindings()];
    }

    @Override
    public VulkanVertexDataBuilder vertices(int binding, ByteBuffer vertices) {
        this.vertices[binding] = vertices;
        return this;
    }

    @Override
    public VulkanVertexDataBuilder indices(ByteBuffer indices) {
        this.indices = indices;
        return this;
    }

    @Override
    public VulkanVertexData build() {
        return new VulkanVertexData(layout, firstVertex, getVertexCount(vertices), vertices, indices);
    }
}
