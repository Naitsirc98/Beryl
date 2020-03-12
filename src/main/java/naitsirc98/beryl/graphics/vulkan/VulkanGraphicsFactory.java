package naitsirc98.beryl.graphics.vulkan;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.graphics.vulkan.textures.VulkanTexture2D;
import naitsirc98.beryl.graphics.vulkan.vertex.VulkanVertexDataBuilder;
import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;

public class VulkanGraphicsFactory implements GraphicsFactory {

    @Override
    public VertexData.Builder newVertexDataBuilder(VertexLayout layout) {
        return new VulkanVertexDataBuilder(layout);
    }

    @Override
    public Texture2D newTexture2D() {
        return new VulkanTexture2D();
    }

    @Override
    public Texture2D blankTexture2D() {
        return null;
    }

    @Override
    public void free() {

    }
}
