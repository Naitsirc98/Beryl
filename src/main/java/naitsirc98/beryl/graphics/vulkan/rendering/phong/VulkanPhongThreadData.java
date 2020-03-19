package naitsirc98.beryl.graphics.vulkan.rendering.phong;

import naitsirc98.beryl.graphics.vulkan.buffers.VulkanCPUBuffer;
import naitsirc98.beryl.graphics.vulkan.buffers.VulkanUniformBuffer;
import naitsirc98.beryl.graphics.vulkan.commands.VulkanThreadData;
import naitsirc98.beryl.graphics.vulkan.descriptors.VulkanDescriptorPool;
import naitsirc98.beryl.graphics.vulkan.descriptors.VulkanDescriptorSetLayout;
import naitsirc98.beryl.graphics.vulkan.textures.VulkanTexture2D;
import naitsirc98.beryl.graphics.vulkan.vertex.VulkanVertexData;
import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.scenes.components.meshes.MeshView;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static naitsirc98.beryl.graphics.Graphics.vulkan;
import static naitsirc98.beryl.util.Maths.roundUp2;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.libc.LibCString.nmemcpy;
import static org.lwjgl.vulkan.VK10.*;

final class VulkanPhongThreadData implements VulkanThreadData {

    private static final int MIN_UNIFORM_BUFFER_OFFSET_ALIGNMENT
            = (int) vulkan().physicalDevice().properties().limits().minUniformBufferOffsetAlignment();

    static final int CAMERA_POSITION_PUSH_CONSTANT_SIZE = 4 * FLOAT32_SIZEOF;
    static final int MVP_PUSH_CONSTANT_SIZE =  4 * 4 * FLOAT32_SIZEOF;
    static final int PUSH_CONSTANT_SIZE = CAMERA_POSITION_PUSH_CONSTANT_SIZE + MVP_PUSH_CONSTANT_SIZE;

    private static final int MATRICES_UNIFORM_BUFFER_SIZE = roundUp2(16 * 2 * FLOAT32_SIZEOF, MIN_UNIFORM_BUFFER_OFFSET_ALIGNMENT);

    private static final int MATERIAL_UNIFORM_BUFFER_SIZE = roundUp2(PhongMaterial.SIZEOF, MIN_UNIFORM_BUFFER_OFFSET_ALIGNMENT);

    private final VkDevice logicalDeviceHandle;

    private final VulkanDescriptorPool matricesDescriptorPool;
    private final VulkanDescriptorPool materialDescriptorPool;
    private final Map<Material, MaterialDescriptorSet> materialDescriptorSets;
    private final LongBuffer pDescriptorSets;
    private final IntBuffer pDynamicOffsets;

    private VulkanUniformBuffer matricesUniformBuffer;
    private long matricesUniformBufferData;
    private VulkanUniformBuffer materialUniformBuffer;

    final Matrix4f matrix;
    final ByteBuffer pushConstantData;
    final long pushConstantDataAddress;
    VulkanVertexData lastVertexData;
    PhongMaterial lastMaterial;

    public VulkanPhongThreadData(VulkanDescriptorSetLayout matricesDescriptorLayout, VulkanDescriptorSetLayout materialDescriptorLayout) {

        logicalDeviceHandle = vulkan().logicalDevice().handle();

        matricesDescriptorPool = createMatricesDescriptorPool(matricesDescriptorLayout, 1);
        materialDescriptorPool = createMaterialDescriptorPool(materialDescriptorLayout, 1);
        materialDescriptorSets = new HashMap<>();

        pDescriptorSets = memAllocLong(3);
        pDynamicOffsets = memAllocInt(2);

        matricesUniformBuffer = new VulkanUniformBuffer(MATRICES_UNIFORM_BUFFER_SIZE);
        materialUniformBuffer = new VulkanUniformBuffer(MATERIAL_UNIFORM_BUFFER_SIZE);
        initMatricesDescriptorSets();

        matrix = new Matrix4f();

        pushConstantData = memAlloc(PUSH_CONSTANT_SIZE);
        pushConstantDataAddress = memAddress(pushConstantData);
    }

    public void begin(List<MeshView> meshViews, Set<Material> materials) {

        if(matricesUniformBuffer.size() < meshViews.size() * MATRICES_UNIFORM_BUFFER_SIZE) {
            reallocateMatricesUniformBuffer(meshViews.size());
        }

        if(materials.stream()
                .filter(material -> material instanceof PhongMaterial)
                .allMatch(materialDescriptorSets::containsKey)) {

            return;
        }

        List<PhongMaterial> newMaterials = materials.stream()
                .unordered()
                .filter(material -> material instanceof PhongMaterial)
                .map(PhongMaterial.class::cast)
                .filter(material -> !materialDescriptorSets.containsKey(material))
                .collect(Collectors.toList());

        if(!newMaterials.isEmpty()) {
            createMaterialsDescriptorSets(newMaterials);
        }
    }

    private void createMaterialsDescriptorSets(List<PhongMaterial> newMaterials) {

        final long newSize = MATERIAL_UNIFORM_BUFFER_SIZE * newMaterials.size();

        VulkanCPUBuffer oldMaterialUniformBuffer = materialUniformBuffer;
        materialUniformBuffer = new VulkanUniformBuffer(newSize);

        VulkanCPUBuffer.copy(oldMaterialUniformBuffer, materialUniformBuffer, oldMaterialUniformBuffer.size());

        int descriptorSetIndex = materialDescriptorSets.size();

        materialDescriptorPool.ensure(newMaterials.size());

        for(PhongMaterial material : newMaterials) {

            final int dynamicOffset = descriptorSetIndex * MATERIAL_UNIFORM_BUFFER_SIZE; // Check alignment
            final long descriptorSet = materialDescriptorPool.descriptorSet(descriptorSetIndex);

            initMaterialDescriptorSet(material, descriptorSet);

            materialDescriptorSets.put(material, new MaterialDescriptorSet(descriptorSet, dynamicOffset));

            try(MemoryStack stack = stackPush()) {
                ByteBuffer buffer = stack.malloc(MATRICES_UNIFORM_BUFFER_SIZE);
                materialUniformBuffer.update(descriptorSetIndex * MATERIAL_UNIFORM_BUFFER_SIZE, material.get(0, buffer));
            }

            ++descriptorSetIndex;
        }
    }

    private void reallocateMatricesUniformBuffer(int matricesCount) {

        if(matricesUniformBufferData != NULL) {
            matricesUniformBuffer.unmapMemory();
            matricesUniformBufferData = NULL;
        }

        final int newSize = matricesCount * MATRICES_UNIFORM_BUFFER_SIZE;

        matricesUniformBuffer.allocate(newSize);

        initMatricesDescriptorSets();

        pDescriptorSets.put(0, matricesDescriptorPool.descriptorSet(0));

        matricesUniformBufferData = matricesUniformBuffer.mapMemory(0).get(0);
    }

    @Override
    public void free() {

        if(matricesUniformBufferData != NULL) {
            matricesUniformBuffer.unmapMemory();
            matricesUniformBufferData = NULL;
        }

        matricesDescriptorPool.free();
        materialDescriptorPool.free();
        matricesUniformBuffer.free();
        materialUniformBuffer.free();
        memFree(pDescriptorSets);
        memFree(pDynamicOffsets);
        memFree(pushConstantData);
    }

    @Override
    public void end() {
        lastVertexData = null;
        lastMaterial = null;
    }

    void bindDescriptorSets(VkCommandBuffer commandBuffer, long pipelineLayout) {
        vkCmdBindDescriptorSets(
                commandBuffer,
                VK_PIPELINE_BIND_POINT_GRAPHICS,
                pipelineLayout,
                0,
                pDescriptorSets,
                pDynamicOffsets);
    }

    void updateUniformData(int index, MeshView meshView, long lightsDescriptorSet) {

        updateMatrices(index, meshView);

        final MaterialDescriptorSet materialDescriptorSet = materialDescriptorSets.get(meshView.mesh().material());

        pDescriptorSets.put(1, materialDescriptorSet.descriptorSet);
        pDescriptorSets.put(2, lightsDescriptorSet);

        pDynamicOffsets.put(1, materialDescriptorSet.dynamicOffset);
    }

    private void updateMatrices(int index, MeshView meshView) {

        final int offset = index * MATRICES_UNIFORM_BUFFER_SIZE; // Check for alignment

        try(MemoryStack stack = stackPush()) {

            ByteBuffer buffer = stack.malloc(MATRICES_UNIFORM_BUFFER_SIZE);

            meshView.modelMatrix().get(0, buffer);
            meshView.normalMatrix().get(16 * FLOAT32_SIZEOF, buffer);

            nmemcpy(matricesUniformBufferData + offset, memAddress(buffer), MATRICES_UNIFORM_BUFFER_SIZE);
            // matricesUniformBuffer.update(offset, buffer);
        }

        pDynamicOffsets.put(0, offset);
    }

    private VulkanDescriptorPool createMatricesDescriptorPool(VulkanDescriptorSetLayout matricesDescriptorLayout, int count) {
        return new VulkanDescriptorPool(
                matricesDescriptorLayout,
                count,
                count,
                VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC);
    }

    private VulkanDescriptorPool createMaterialDescriptorPool(VulkanDescriptorSetLayout materialDescriptorLayout, int count) {
        return new VulkanDescriptorPool(
                materialDescriptorLayout,
                count,
                count,
                VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC,
                VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    }

    private void initMatricesDescriptorSets() {

        try(MemoryStack stack = stackPush()) {

            VkDescriptorBufferInfo.Buffer bufferInfos = VkDescriptorBufferInfo.callocStack(1, stack)
                    .buffer(matricesUniformBuffer.handle())
                    .offset(0)
                    .range(MATRICES_UNIFORM_BUFFER_SIZE);

            VkWriteDescriptorSet.Buffer writeDescriptorSets = VkWriteDescriptorSet.callocStack(matricesDescriptorPool.size(), stack);

            for(int i = 0;i < writeDescriptorSets.capacity();i++) {
                writeDescriptorSets.get(i)
                        .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                        .dstBinding(0)
                        .dstArrayElement(0)
                        .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC)
                        .descriptorCount(1)
                        .dstSet(matricesDescriptorPool.descriptorSet(i))
                        .pBufferInfo(bufferInfos);
            }

            vkUpdateDescriptorSets(logicalDeviceHandle, writeDescriptorSets, null);
        }
    }

    private void initMaterialDescriptorSet(PhongMaterial material, long descriptorSet) {

        try(MemoryStack stack = stackPush()) {

            VkDescriptorImageInfo.Buffer imageInfos = VkDescriptorImageInfo.callocStack(4, stack);

            final VulkanTexture2D[] textures = new VulkanTexture2D[] {
                    material.ambientMap(),
                    material.diffuseMap(),
                    material.specularMap(),
                    material.emissiveMap(),
            };

            for(int i = 0;i < imageInfos.capacity();i++) {
                imageInfos.get(i)
                        .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                        .imageView(textures[i].view().handle())
                        .sampler(textures[i].sampler().handle());
            }

            VkDescriptorBufferInfo.Buffer bufferInfos = VkDescriptorBufferInfo.callocStack(1, stack)
                    .buffer(materialUniformBuffer.handle())
                    .offset(0)
                    .range(MATERIAL_UNIFORM_BUFFER_SIZE);

            VkWriteDescriptorSet.Buffer writeDescriptorSets = VkWriteDescriptorSet.callocStack(5, stack);

            // Uniform Buffer
            writeDescriptorSets.get(0)
                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstBinding(1)
                    .dstArrayElement(0)
                    .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC)
                    .descriptorCount(1)
                    .dstSet(descriptorSet)
                    .pBufferInfo(bufferInfos);

            // Ambient Map
            writeDescriptorSets.get(1)
                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstBinding(2)
                    .dstArrayElement(0)
                    .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                    .descriptorCount(1)
                    .dstSet(descriptorSet)
                    .pImageInfo(VkDescriptorImageInfo.callocStack(1).put(0, imageInfos.get(0)));

            // Diffuse Map
            writeDescriptorSets.get(2)
                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstBinding(3)
                    .dstArrayElement(0)
                    .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                    .descriptorCount(1)
                    .dstSet(descriptorSet)
                    .pImageInfo(VkDescriptorImageInfo.callocStack(1).put(0, imageInfos.get(1)));

            // Specular Map
            writeDescriptorSets.get(3)
                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstBinding(4)
                    .dstArrayElement(0)
                    .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                    .descriptorCount(1)
                    .dstSet(descriptorSet)
                    .pImageInfo(VkDescriptorImageInfo.callocStack(1).put(0, imageInfos.get(2)));

            // Emissive Map
            writeDescriptorSets.get(4)
                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstBinding(5)
                    .dstArrayElement(0)
                    .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                    .descriptorCount(1)
                    .dstSet(descriptorSet)
                    .pImageInfo(VkDescriptorImageInfo.callocStack(1).put(0, imageInfos.get(3)));


            vkUpdateDescriptorSets(logicalDeviceHandle, writeDescriptorSets, null);
        }
    }

    private static final class MaterialDescriptorSet {

        private final long descriptorSet;
        private final int dynamicOffset;

        public MaterialDescriptorSet(long descriptorSet, int dynamicOffset) {
            this.descriptorSet = descriptorSet;
            this.dynamicOffset = dynamicOffset;
        }
    }

}