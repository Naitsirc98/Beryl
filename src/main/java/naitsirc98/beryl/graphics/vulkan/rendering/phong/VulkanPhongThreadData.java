package naitsirc98.beryl.graphics.vulkan.rendering.phong;

import naitsirc98.beryl.graphics.vulkan.commands.VulkanThreadData;
import naitsirc98.beryl.graphics.vulkan.descriptors.VulkanDescriptorPool;
import naitsirc98.beryl.graphics.vulkan.descriptors.VulkanDescriptorSetLayout;
import naitsirc98.beryl.graphics.vulkan.descriptors.VulkanDescriptorSets;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;

final class VulkanPhongThreadData implements VulkanThreadData {

    private static final int PHONG_MATERIAL_TEXTURE_COUNT = 4;

    final VulkanDescriptorPool descriptorPool;
    final VulkanDescriptorSets descriptorSets;
    final VkDescriptorBufferInfo.Buffer descriptorBufferInfos;
    final VkDescriptorImageInfo.Buffer descriptorImageInfos;
    final VkWriteDescriptorSet.Buffer writeDescriptorSets;

    public VulkanPhongThreadData(int count, VulkanDescriptorSetLayout descriptorSetLayout) {
        descriptorPool = new VulkanDescriptorPool();
        descriptorSets = new VulkanDescriptorSets(descriptorPool, descriptorSetLayout, count);
        descriptorBufferInfos = getDescriptorBufferInfos();
        descriptorImageInfos = getDescriptorImageInfos();
        writeDescriptorSets = VkWriteDescriptorSet.calloc(2);
    }

    private VkDescriptorBufferInfo.Buffer getDescriptorBufferInfos() {
        return VkDescriptorBufferInfo.calloc(1)
                .offset(0);
    }

    private VkDescriptorImageInfo.Buffer getDescriptorImageInfos() {
        return VkDescriptorImageInfo.calloc(PHONG_MATERIAL_TEXTURE_COUNT)
                .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
    }

    @Override
    public void free() {
        descriptorSets.free();
        descriptorPool.free();
    }
}
