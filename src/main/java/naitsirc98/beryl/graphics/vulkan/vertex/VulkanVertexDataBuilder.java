package naitsirc98.beryl.graphics.vulkan.vertex;

import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;

import java.nio.ByteBuffer;

public class VulkanVertexDataBuilder extends VertexData.Builder {

    public VulkanVertexDataBuilder(VertexLayout layout) {
        super(layout);
    }

    @Override
    public VertexData.Builder vertices(int binding, ByteBuffer vertices) {
        return null;
    }

    @Override
    public VertexData.Builder indices(ByteBuffer indices) {
        return null;
    }

    @Override
    public VertexData build() {
        return null;
    }
}
