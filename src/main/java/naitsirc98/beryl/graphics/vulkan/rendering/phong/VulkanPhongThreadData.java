package naitsirc98.beryl.graphics.vulkan.rendering.phong;

import naitsirc98.beryl.graphics.vulkan.buffers.VulkanUniformBuffer;
import naitsirc98.beryl.graphics.vulkan.commands.VulkanThreadData;
import naitsirc98.beryl.graphics.vulkan.descriptors.VulkanDescriptorPool;
import naitsirc98.beryl.graphics.vulkan.descriptors.VulkanDescriptorSetLayout;
import naitsirc98.beryl.graphics.vulkan.descriptors.VulkanDescriptorSets;
import naitsirc98.beryl.graphics.vulkan.rendering.VulkanRenderer;
import naitsirc98.beryl.graphics.vulkan.textures.VulkanTexture2D;
import naitsirc98.beryl.graphics.vulkan.vertex.VulkanVertexData;
import naitsirc98.beryl.materials.PhongMaterial;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;

import static naitsirc98.beryl.graphics.Graphics.vulkan;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.vulkan.VK10.*;

final class VulkanPhongThreadData implements VulkanThreadData {

    private static final int PHONG_MATERIAL_TEXTURE_COUNT = 4;
    static final int PUSH_CONSTANT_DATA_SIZE = 4 * 4 * FLOAT32_SIZEOF;

    private final VulkanDescriptorPool descriptorPool;
    private final VulkanDescriptorSets descriptorSets;
    private final VkDescriptorBufferInfo.Buffer descriptorBufferInfos;
    private final VkDescriptorImageInfo.Buffer[] descriptorImageInfos;
    private final VkWriteDescriptorSet.Buffer writeDescriptorSets;
    private final VulkanUniformBuffer[] uniformBuffers;
    private final VkDevice logicalDeviceHandle;
    final Matrix4f matrix;
    final ByteBuffer pushConstantData;
    final long pushConstantDataAddress;
    VulkanVertexData lastVertexData;
    PhongMaterial lastMaterial;

    public VulkanPhongThreadData(VulkanDescriptorSetLayout descriptorSetLayout) {
        final int swapchainImageCount = vulkan().swapchain().imageCount();
        descriptorPool = new VulkanDescriptorPool();
        descriptorSets = new VulkanDescriptorSets(descriptorPool, descriptorSetLayout, swapchainImageCount);
        descriptorBufferInfos = getDescriptorBufferInfos();
        descriptorImageInfos = getDescriptorImageInfos();
        writeDescriptorSets = createWriteDescriptorSets();
        logicalDeviceHandle = vulkan().logicalDevice().handle();
        uniformBuffers = createUniformBuffers(swapchainImageCount);
        matrix = new Matrix4f();
        pushConstantData = memAlloc(PUSH_CONSTANT_DATA_SIZE);
        pushConstantDataAddress = memAddress(pushConstantData);
    }

    void bindDescriptorSets(VkCommandBuffer commandBuffer, long pipelineLayout) {
        vkCmdBindDescriptorSets(commandBuffer,
                VK_PIPELINE_BIND_POINT_GRAPHICS,
                pipelineLayout,
                0,
                descriptorSets.pDescriptorSets(),
                null);
    }

    void updateMaterialShaderData(PhongMaterial material) {

        final int index = VulkanRenderer.get().currentSwapchainImageIndex();
        final long descriptorSet = descriptorSets.get(index);

        setUniformBufferInfo(index, material);
        setTextureInfo(material);

        uniformBufferWriteDescriptorSet().dstSet(descriptorSet);
        samplerWriteDescriptorSet().dstSet(descriptorSet);

        vkUpdateDescriptorSets(logicalDeviceHandle, writeDescriptorSets, null);
    }

    private VkWriteDescriptorSet uniformBufferWriteDescriptorSet() {
        return writeDescriptorSets.get(0);
    }

    private VkWriteDescriptorSet samplerWriteDescriptorSet() {
        return writeDescriptorSets.get(1);
    }

    private void setTextureInfo(PhongMaterial material) {
        setTextureInfo(0, material.ambientMap());
        setTextureInfo(1, material.diffuseMap());
        setTextureInfo(2, material.specularMap());
        setTextureInfo(3, material.emissiveMap());
    }

    private void setTextureInfo(int imageInfoIndex, VulkanTexture2D texture) {
        descriptorImageInfos[imageInfoIndex]
                .imageView(texture.view().handle())
                .sampler(texture.sampler().handle());
    }

    private void setUniformBufferInfo(int index, PhongMaterial material) {
        try(MemoryStack stack = stackPush()) {
            final FloatBuffer uniformBufferData = material.get(stack.mallocFloat(PhongMaterial.FLOAT_BUFFER_MIN_SIZE));
            uniformBuffers[index].update(0, uniformBufferData.rewind());
        }
    }

    private VkDescriptorBufferInfo.Buffer getDescriptorBufferInfos() {
        return VkDescriptorBufferInfo.calloc(1)
                .offset(0);
    }

    private VkDescriptorImageInfo.Buffer[] getDescriptorImageInfos() {

        VkDescriptorImageInfo.Buffer[] descriptorImageInfos = new VkDescriptorImageInfo.Buffer[PHONG_MATERIAL_TEXTURE_COUNT];

        for(int i = 0;i < PHONG_MATERIAL_TEXTURE_COUNT;i++) {
            descriptorImageInfos[i] = VkDescriptorImageInfo.calloc(1).imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        }

        return descriptorImageInfos;
    }

    private VkWriteDescriptorSet.Buffer createWriteDescriptorSets() {

        VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.calloc(PHONG_MATERIAL_TEXTURE_COUNT + 1);

        descriptorWrites.get(0)
                .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                .dstBinding(0)
                .dstArrayElement(0)
                .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .descriptorCount(1)
                .pBufferInfo(descriptorBufferInfos);

        for(int i = 1;i < descriptorWrites.capacity();i++) {
            descriptorWrites.get(i)
                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstBinding(i)
                    .dstArrayElement(0)
                    .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                    .descriptorCount(1)
                    .pImageInfo(descriptorImageInfos[i]);
        }

        return writeDescriptorSets;
    }

    private VulkanUniformBuffer[] createUniformBuffers(int count) {

        VulkanUniformBuffer[] uniformBuffers = new VulkanUniformBuffer[count];

        for(int i = 0;i < count;i++) {
            uniformBuffers[i] = new VulkanUniformBuffer(PhongMaterial.SIZEOF);
        }

        return uniformBuffers;
    }

    @Override
    public void free() {
        descriptorSets.free();
        descriptorPool.free();
        descriptorBufferInfos.free();
        Arrays.stream(descriptorImageInfos).forEach(VkDescriptorImageInfo.Buffer::free);
        writeDescriptorSets.free();
        Arrays.stream(uniformBuffers).forEach(VulkanUniformBuffer::free);
    }

    @Override
    public void end() {
        lastVertexData = null;
        lastMaterial = null;
    }
}
