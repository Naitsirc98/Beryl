package naitsirc98.beryl.graphics.vulkan.vertex;

import naitsirc98.beryl.meshes.vertices.VertexAttribute;
import naitsirc98.beryl.meshes.vertices.VertexAttributeList;
import naitsirc98.beryl.meshes.vertices.VertexAttributeList.VertexAttributeIterator;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanFormatUtils.asVkFormat;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_INSTANCE;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;

public final class VulkanVertexInputUtils {

    public static VkVertexInputBindingDescription.Buffer vertexInputBindingsStack(VertexLayout layout) {

        VkVertexInputBindingDescription.Buffer bindingDescriptions = VkVertexInputBindingDescription.callocStack(layout.bindings());

        for(int i = 0;i < layout.bindings();i++) {
            VertexAttributeList attributes = layout.attributeList(i);
            bindingDescriptions.get(i).set(i, attributes.stride(), getVkInputRate(attributes));
        }

        return bindingDescriptions;
    }

    public static VkVertexInputAttributeDescription.Buffer vertexInputAttributesStack(VertexLayout layout) {

        int descriptionCount = 0;

        for(int i = 0;i < layout.bindings();i++) {
            descriptionCount += layout.attributeList(i).count();
        }

        var descriptions = VkVertexInputAttributeDescription.callocStack(descriptionCount);

        for(int binding = 0;binding < layout.bindings();binding++) {

            VertexAttributeList attributes = layout.attributeList(binding);

            VertexAttributeIterator iterator = attributes.iterator();

            for(int i = 0;i < attributes.count();i++) {
                VertexAttribute attribute = iterator.next();
                descriptions.get(i).set(iterator.location(), binding, getVkFormat(attribute), iterator.offset());
            }
        }

        return descriptions;
    }

    private static int getVkFormat(VertexAttribute attribute) {
        return asVkFormat(attribute.dataType(), attribute.size());
    }

    private static int getVkInputRate(VertexAttributeList attributes) {
        return attributes.instanced() ? VK_VERTEX_INPUT_RATE_INSTANCE : VK_VERTEX_INPUT_RATE_VERTEX;
    }

    private VulkanVertexInputUtils() {}
}
