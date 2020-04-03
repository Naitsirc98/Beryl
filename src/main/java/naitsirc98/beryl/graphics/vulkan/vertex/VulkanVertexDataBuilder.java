package naitsirc98.beryl.graphics.vulkan.vertex;

import naitsirc98.beryl.graphics.rendering.PrimitiveTopology;
import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import naitsirc98.beryl.util.types.DataType;

import java.nio.ByteBuffer;

public class VulkanVertexDataBuilder extends VertexData.Builder {

    private ByteBuffer[] vertices;
    private ByteBuffer indices;

    public VulkanVertexDataBuilder(VertexLayout layout, PrimitiveTopology primitiveTopology) {
        super(layout, primitiveTopology);
        vertices = new ByteBuffer[layout.bindings()];
    }

    @Override
    public VulkanVertexDataBuilder vertices(int binding, ByteBuffer vertices) {
        this.vertices[binding] = vertices;
        return this;
    }

    @Override
    public VulkanVertexDataBuilder indices(ByteBuffer indices, DataType indexType) {
        this.indices = indices;
        this.indexCount = indices.remaining() / indexType.sizeof();
        return this;
    }

    @Override
    public VulkanVertexData build() {
        return new VulkanVertexData(layout, primitiveTopology, firstVertex, getVertexCount(vertices), vertices, indices, indexCount);
    }
}
