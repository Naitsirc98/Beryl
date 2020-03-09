package naitsirc98.beryl.graphics.vulkan;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.vulkan.vertex.VulkanVertexDataBuilder;
import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;

public class VulkanGraphicsFactory implements GraphicsFactory {

    @Override
    public VertexData.Builder newVertexDataBuilder(VertexLayout layout) {
        return new VulkanVertexDataBuilder(layout);
    }
}
