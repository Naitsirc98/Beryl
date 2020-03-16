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
import naitsirc98.beryl.scenes.components.meshes.MeshView;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;

import static naitsirc98.beryl.graphics.Graphics.vulkan;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

final class VulkanPhongThreadData implements VulkanThreadData {

    static final int PUSH_CONSTANT_DATA_SIZE = 4 * 4 * FLOAT32_SIZEOF;

    private static final int UNIFORM_BUFFERS_COUNT = 2;
    private static final int PHONG_MATERIAL_TEXTURE_COUNT = 4;

    private static final int MATRICES_UNIFORM_BUFFER_INDEX = 0;
    private static final int MATERIAL_UNIFORM_BUFFER_INDEX = 1;

    private static final int MATRICES_UNIFORM_BUFFER_SIZE = 16 * 2 * FLOAT32_SIZEOF;

    private final VkDevice logicalDeviceHandle;

    private final VulkanDescriptorPool descriptorPool;
    private final VulkanDescriptorSets descriptorSets;
    private final LongBuffer pDescriptorSet;

    private final VkDescriptorBufferInfo.Buffer[] descriptorBufferInfos;
    private final VkDescriptorImageInfo.Buffer[] descriptorImageInfos;
    private final VkWriteDescriptorSet.Buffer writeDescriptorSets;

    private final VulkanUniformBuffer[] uniformBuffers;
    private final FloatBuffer uniformBufferData;

    final Matrix4f matrix;
    final ByteBuffer pushConstantData;
    final long pushConstantDataAddress;
    VulkanVertexData lastVertexData;
    PhongMaterial lastMaterial;

    public VulkanPhongThreadData(VulkanDescriptorSetLayout descriptorSetLayout) {
        final int swapchainImageCount = vulkan().swapchain().imageCount();
        descriptorPool = createDescriptorPool(swapchainImageCount);
        descriptorSets = new VulkanDescriptorSets(descriptorPool, descriptorSetLayout, swapchainImageCount);
        pDescriptorSet = memAllocLong(1);
        descriptorBufferInfos = getDescriptorBufferInfos();
        descriptorImageInfos = getDescriptorImageInfos();
        writeDescriptorSets = createWriteDescriptorSets();
        logicalDeviceHandle = vulkan().logicalDevice().handle();
        uniformBuffers = VulkanUniformBuffer.create(swapchainImageCount, PhongMaterial.SIZEOF + MATRICES_UNIFORM_BUFFER_SIZE);
        matrix = new Matrix4f();
        pushConstantData = memAlloc(PUSH_CONSTANT_DATA_SIZE);
        pushConstantDataAddress = memAddress(pushConstantData);
        uniformBufferData = memAllocFloat((MATRICES_UNIFORM_BUFFER_SIZE + PhongMaterial.SIZEOF) / FLOAT32_SIZEOF);
    }

    @Override
    public void free() {
        descriptorSets.free();
        descriptorPool.free();
        Arrays.stream(descriptorBufferInfos).forEach(VkDescriptorBufferInfo.Buffer::free);
        Arrays.stream(descriptorImageInfos).forEach(VkDescriptorImageInfo.Buffer::free);
        writeDescriptorSets.free();
        Arrays.stream(uniformBuffers).forEach(VulkanUniformBuffer::free);
        memFree(uniformBufferData);
        memFree(pushConstantData);
    }

    @Override
    public void end() {
        lastVertexData = null;
        lastMaterial = null;
    }

    void updateMeshData(VkCommandBuffer commandBuffer, MeshView meshView, long pipelineLayout) {

        final int index = VulkanRenderer.get().currentSwapchainImageIndex();
        final long descriptorSet = descriptorSets.get(index);
        final VulkanUniformBuffer uniformBuffer = uniformBuffers[index];
        final PhongMaterial material = meshView.mesh().material();

        setMatricesUniformBufferInfo(descriptorSet, uniformBuffer, meshView.modelMatrix(), meshView.normalMatrix());

        if(material != lastMaterial) {
            setMaterialUniformBufferInfo(descriptorSet, uniformBuffer, material);
            setTextureInfo(descriptorSet, material);
            lastMaterial = material;
        }

        vkUpdateDescriptorSets(logicalDeviceHandle, writeDescriptorSets, null);

        vkCmdBindDescriptorSets(
                commandBuffer,
                VK_PIPELINE_BIND_POINT_GRAPHICS,
                pipelineLayout,
                0,
                pDescriptorSet.put(0, currentDescriptorSet()),
                null);
    }

    private void setMatricesUniformBufferInfo(long descriptorSet, VulkanUniformBuffer uniformBuffer,
                                              Matrix4fc modelMatrix, Matrix4fc normalMatrix) {

        final FloatBuffer uniformBufferData = this.uniformBufferData;

        modelMatrix.get(0, uniformBufferData);
        normalMatrix.get(16, uniformBufferData);

        uniformBuffer.update(0, uniformBufferData);

        descriptorBufferInfos[MATRICES_UNIFORM_BUFFER_INDEX].buffer(uniformBuffer.handle());
        writeDescriptorSets.get(MATRICES_UNIFORM_BUFFER_INDEX).dstSet(descriptorSet);
    }

    private void setTextureInfo(long descriptorSet, PhongMaterial material) {
        setTextureInfo(descriptorSet, 0, material.ambientMap());
        setTextureInfo(descriptorSet, 1, material.diffuseMap());
        setTextureInfo(descriptorSet, 2, material.specularMap());
        setTextureInfo(descriptorSet, 3, material.emissiveMap());
    }

    private void setTextureInfo(long descriptorSet, int imageInfoIndex, VulkanTexture2D texture) {

        texture.validate();

        descriptorImageInfos[imageInfoIndex]
                .imageView(texture.view().handle())
                .sampler(texture.sampler().handle());

        writeDescriptorSets.get(imageInfoIndex + 1).dstSet(descriptorSet);
    }

    private void setMaterialUniformBufferInfo(long descriptorSet, VulkanUniformBuffer uniformBuffer, PhongMaterial material) {

        try(MemoryStack stack = stackPush()) {
            final FloatBuffer uniformBufferData = material.get(stack.mallocFloat(PhongMaterial.FLOAT_BUFFER_MIN_SIZE));
            uniformBuffer.update(MATRICES_UNIFORM_BUFFER_SIZE, uniformBufferData.rewind());
        }

        descriptorBufferInfos[MATERIAL_UNIFORM_BUFFER_INDEX].buffer(uniformBuffer.handle());
        writeDescriptorSets.get(MATERIAL_UNIFORM_BUFFER_INDEX).dstSet(descriptorSet);
    }

    private long currentDescriptorSet() {
        return descriptorSets.get(VulkanRenderer.get().currentSwapchainImageIndex());
    }

    private VkDescriptorBufferInfo.Buffer[] getDescriptorBufferInfos() {

        VkDescriptorBufferInfo.Buffer[] descriptorBufferInfos =  new VkDescriptorBufferInfo.Buffer[2];

        descriptorBufferInfos[0] = VkDescriptorBufferInfo.calloc(1).offset(0).range(MATRICES_UNIFORM_BUFFER_SIZE);

        descriptorBufferInfos[1] = VkDescriptorBufferInfo.calloc(1).offset(MATRICES_UNIFORM_BUFFER_SIZE).range(PhongMaterial.SIZEOF);

        return descriptorBufferInfos;
    }

    private VkDescriptorImageInfo.Buffer[] getDescriptorImageInfos() {

        VkDescriptorImageInfo.Buffer[] descriptorImageInfos = new VkDescriptorImageInfo.Buffer[PHONG_MATERIAL_TEXTURE_COUNT];

        for(int i = 0;i < PHONG_MATERIAL_TEXTURE_COUNT;i++) {
            descriptorImageInfos[i] = VkDescriptorImageInfo.calloc(1).imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        }

        return descriptorImageInfos;
    }

    private VkWriteDescriptorSet.Buffer createWriteDescriptorSets() {

        VkWriteDescriptorSet.Buffer writeDescriptorSets = VkWriteDescriptorSet.calloc(UNIFORM_BUFFERS_COUNT + PHONG_MATERIAL_TEXTURE_COUNT);

        writeDescriptorSets.get(MATRICES_UNIFORM_BUFFER_INDEX)
                .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                .dstBinding(0)
                .dstArrayElement(0)
                .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .descriptorCount(1)
                .pBufferInfo(descriptorBufferInfos[MATRICES_UNIFORM_BUFFER_INDEX]);

        writeDescriptorSets.get(MATERIAL_UNIFORM_BUFFER_INDEX)
                .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                .dstBinding(1)
                .dstArrayElement(0)
                .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .descriptorCount(1)
                .pBufferInfo(descriptorBufferInfos[MATERIAL_UNIFORM_BUFFER_INDEX]);

        for(int i = 1;i < writeDescriptorSets.capacity();i++) {
            writeDescriptorSets.get(i)
                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstBinding(i)
                    .dstArrayElement(0)
                    .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                    .descriptorCount(1)
                    .pImageInfo(descriptorImageInfos[i - 1]);
        }

        return writeDescriptorSets;
    }

    private VulkanDescriptorPool createDescriptorPool(int maxSets) {
        return new VulkanDescriptorPool(
                maxSets,
                VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    }

}