package naitsirc98.beryl.graphics.vulkan.rendering.phong;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.graphics.vulkan.buffers.VulkanMappableBuffer;
import naitsirc98.beryl.graphics.vulkan.buffers.VulkanUniformBuffer;
import naitsirc98.beryl.graphics.vulkan.commands.VulkanCommandBufferRecorder;
import naitsirc98.beryl.graphics.vulkan.commands.VulkanCommandBufferThreadExecutor;
import naitsirc98.beryl.graphics.vulkan.commands.VulkanThreadData;
import naitsirc98.beryl.graphics.vulkan.descriptors.VulkanDescriptorPool;
import naitsirc98.beryl.graphics.vulkan.descriptors.VulkanDescriptorSetLayout;
import naitsirc98.beryl.graphics.vulkan.pipelines.VulkanGraphicsPipeline;
import naitsirc98.beryl.graphics.vulkan.pipelines.VulkanPipelineLayout;
import naitsirc98.beryl.graphics.vulkan.pipelines.VulkanShaderModule;
import naitsirc98.beryl.graphics.vulkan.rendering.VulkanRenderer;
import naitsirc98.beryl.graphics.vulkan.swapchain.VulkanSwapchain;
import naitsirc98.beryl.graphics.vulkan.swapchain.VulkanSwapchainDependent;
import naitsirc98.beryl.graphics.vulkan.textures.VulkanTexture2D;
import naitsirc98.beryl.graphics.vulkan.vertex.VulkanVertexData;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.lights.Light;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.SceneEnvironment;
import naitsirc98.beryl.scenes.components.camera.Camera;
import naitsirc98.beryl.scenes.components.meshes.MeshView;
import naitsirc98.beryl.util.Color;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static naitsirc98.beryl.graphics.vulkan.vertex.VulkanVertexInputUtils.vertexInputAttributesStack;
import static naitsirc98.beryl.graphics.vulkan.vertex.VulkanVertexInputUtils.vertexInputBindingsStack;
import static naitsirc98.beryl.util.Maths.roundUp2;
import static naitsirc98.beryl.util.handles.LongHandle.NULL;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.INT32_SIZEOF;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memAddress0;
import static org.lwjgl.system.libc.LibCString.nmemcpy;
import static org.lwjgl.util.vma.Vma.vmaFlushAllocation;
import static org.lwjgl.vulkan.VK10.*;

public final class VulkanPhongRenderingPath extends RenderingPath
        implements VulkanObject, VulkanCommandBufferRecorder<VulkanPhongThreadData>, VulkanSwapchainDependent {

    public static final VertexLayout VERTEX_LAYOUT = VertexLayout.VERTEX_LAYOUT_3D;

    private static final int RENDER_SUBPASS = 0;

    private static final Path VERTEX_SHADER_PATH;
    private static final Path FRAGMENT_SHADER_PATH;

    private static final int MAX_POINT_LIGHTS = 10;
    private static final int MAX_SPOT_LIGHTS = 10;
    private static final int LIGHTS_UNIFORM_BUFFER_SIZE = (1 + MAX_POINT_LIGHTS + MAX_SPOT_LIGHTS) * Light.SIZEOF + INT32_SIZEOF * 2 + FLOAT32_SIZEOF * 4;
    private static final int DIRECTIONAL_LIGHT_OFFSET = 0;
    private static final int POINT_LIGHTS_OFFSET = Light.SIZEOF;
    private static final int SPOT_LIGHTS_OFFSET = POINT_LIGHTS_OFFSET + Light.SIZEOF * MAX_POINT_LIGHTS;
    private static final int AMBIENT_COLOR_OFFSET = SPOT_LIGHTS_OFFSET + Light.SIZEOF * MAX_SPOT_LIGHTS;
    private static final int POINT_LIGHTS_COUNT_OFFSET = AMBIENT_COLOR_OFFSET + FLOAT32_SIZEOF * 4;
    private static final int SPOT_LIGHTS_COUNT_OFFSET = POINT_LIGHTS_COUNT_OFFSET + INT32_SIZEOF;

    private static final int MIN_UNIFORM_BUFFER_OFFSET_ALIGNMENT
            = (int) Graphics.vulkan().physicalDevice().properties().limits().minUniformBufferOffsetAlignment();

    // MVP, ModelMatrix, NormalMatrix, CameraPosition
    static final int MATRICES_UNIFORM_BUFFER_SIZE = roundUp2((16 + 16 + 16 + 4) * FLOAT32_SIZEOF, MIN_UNIFORM_BUFFER_OFFSET_ALIGNMENT);
    private static final String MATRICES_UNIFORM_BUFFER_NAME = "MatricesUniformBuffer";
    private static final int MATRICES_UNIFORM_BUFFER_MVP_OFFSET = 0;
    private static final int MATRICES_UNIFORM_BUFFER_MODEL_MATRIX_OFFSET = 16 * FLOAT32_SIZEOF;
    private static final int MATRICES_UNIFORM_BUFFER_NORMAL_MATRIX_OFFSET = 32 * FLOAT32_SIZEOF;
    private static final int MATRICES_UNIFORM_BUFFER_CAMERA_POSITION_OFFSET = 48 * FLOAT32_SIZEOF;

    private static final int MATERIAL_UNIFORM_BUFFER_SIZE = roundUp2(PhongMaterial.SIZEOF, MIN_UNIFORM_BUFFER_OFFSET_ALIGNMENT);

    static {

        Path vertexPath = null;
        Path fragmentPath = null;

        try {
            vertexPath = BerylFiles.getPath("shaders/phong/phong.vert");
            fragmentPath = BerylFiles.getPath("shaders/phong/phong.frag");
        } catch (Exception e) {
            Log.fatal("Failed to get shader files for RenderingPath", e);
        }

        VERTEX_SHADER_PATH = vertexPath;
        FRAGMENT_SHADER_PATH = fragmentPath;
    }

    private VulkanPipelineLayout pipelineLayout;
    private VulkanGraphicsPipeline graphicsPipeline;
    private VulkanDescriptorSetLayout matricesDescriptorSetLayout;
    private VulkanDescriptorSetLayout materialDescriptorSetLayout;
    private VulkanDescriptorSetLayout lightsDescriptorSetLayout;
    private VulkanSwapchain swapchain;
    private VulkanCommandBufferThreadExecutor<VulkanPhongThreadData> commandBuffersExecutor;

    private VulkanDescriptorPool lightsDescriptorPool;
    private long lightsDescriptorSet;
    private VulkanUniformBuffer lightsUniformBuffer;
    private long lightsUniformBufferData;

    private VulkanDescriptorPool matricesDescriptorPool;
    private long matricesDescriptorSet;
    private VulkanUniformBuffer matricesUniformBuffer;
    private long matricesUniformBufferData;

    private VulkanDescriptorPool materialDescriptorPool;
    private VulkanUniformBuffer materialUniformBuffer;
    private Map<Material, MaterialDescriptorInfo> materialDescriptorInfos;

    private Matrix4f projectionViewMatrix;
    private List<MeshView> meshViews;
    private Camera camera;
    private int[] sceneModifications;

    private VkCommandBufferInheritanceInfo inheritanceInfo;
    private VkCommandBufferBeginInfo beginInfo;

    private VkCommandBuffer[] primaryCommandBuffers;

    private VulkanPhongRenderingPath() {

    }

    @Override
    protected void init() {

        swapchain = Graphics.vulkan().swapchain();
        swapchain.addSwapchainDependent(this);

        createDescriptorSetLayouts();

        createPipelineLayout();
        createGraphicsPipeline();

        lightsDescriptorPool = new VulkanDescriptorPool(lightsDescriptorSetLayout, 1, 1, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
        lightsDescriptorSet = lightsDescriptorPool.descriptorSet(0);

        matricesDescriptorPool = createMatricesDescriptorPool(1);
        matricesDescriptorSet = matricesDescriptorPool.descriptorSet(0);

        materialDescriptorPool = createMaterialDescriptorPool(1);
        materialDescriptorInfos = new HashMap<>();

        lightsUniformBuffer = new VulkanUniformBuffer(LIGHTS_UNIFORM_BUFFER_SIZE);
        lightsUniformBufferData = lightsUniformBuffer.mapMemory(0).get(0);
        initLightsDescriptorSet();

        matricesUniformBuffer = new VulkanUniformBuffer(MATRICES_UNIFORM_BUFFER_SIZE);
        matricesUniformBufferData = matricesUniformBuffer.mapMemory(0).get(0);
        initMatricesDescriptorSets();

        materialUniformBuffer = new VulkanUniformBuffer(MATERIAL_UNIFORM_BUFFER_SIZE);

        commandBuffersExecutor = new VulkanCommandBufferThreadExecutor<>(this::createThreadData);
        projectionViewMatrix = new Matrix4f();

        primaryCommandBuffers = graphicsCommandPool().newPrimaryCommandBuffers(swapchain.imageCount());

        sceneModifications = new int[swapchain.imageCount()];
        Arrays.fill(sceneModifications, Integer.MIN_VALUE);
    }

    @Override
    protected void terminate() {
        release();
    }

    @Override
    public void release() {

        if(lightsUniformBufferData != NULL) {
            lightsUniformBuffer.unmapMemory();
            lightsUniformBufferData = NULL;
        }

        if(matricesUniformBufferData != NULL) {
            matricesUniformBuffer.unmapMemory();
            matricesUniformBufferData = NULL;
        }

        graphicsCommandPool().freeCommandBuffers(primaryCommandBuffers);

        materialDescriptorInfos.clear();

        commandBuffersExecutor.release();

        pipelineLayout.release();
        graphicsPipeline.release();

        lightsDescriptorPool.release();
        matricesDescriptorPool.release();
        materialDescriptorPool.release();

        lightsUniformBuffer.release();
        matricesUniformBuffer.release();
        materialUniformBuffer.release();

        matricesDescriptorSetLayout.release();
        materialDescriptorSetLayout.release();
        lightsDescriptorSetLayout.release();
    }

    @Override
    public void onSwapchainRecreate() {

        if(lightsUniformBufferData != NULL) {
            lightsUniformBuffer.unmapMemory();
            lightsUniformBufferData = lightsUniformBuffer.mapMemory(0).get(0);
        }

        Arrays.fill(sceneModifications, Integer.MIN_VALUE);

        graphicsCommandPool().freeCommandBuffers(primaryCommandBuffers);

        primaryCommandBuffers = graphicsCommandPool().newPrimaryCommandBuffers(swapchain.imageCount());

        pipelineLayout.release();
        graphicsPipeline.release();

        createPipelineLayout();
        createGraphicsPipeline();
    }

    @Override
    public void render(Camera camera, Scene scene) {

        final List<MeshView> meshViews = scene.meshInfo().meshViews();

        if(meshViews.size() == 0) {
            return;
        }

        this.camera = camera;
        this.meshViews = meshViews;

        projectionViewMatrix.set(camera.projectionViewMatrix());

        try(MemoryStack stack = stackPush()) {

            updateLightsUniformBuffer(scene.environment(), stack);

            updateMatricesUniformBuffer(meshViews);

            updateMaterialInformation(scene.meshInfo().materials(), stack);

            final int imageIndex = VulkanRenderer.get().currentSwapchainImageIndex();

            VkCommandBuffer primaryCommandBuffer = primaryCommandBuffers[imageIndex];

            if(scene.meshInfo().modifications() != sceneModifications[imageIndex]) {

                setupCommandBufferInfos(stack);

                beginPrimaryCommandBuffer(primaryCommandBuffer);

                commandBuffersExecutor.recordCommandBuffers(meshViews.size(), primaryCommandBuffer, this);

                endPrimaryCommandBuffer(primaryCommandBuffer);

                sceneModifications[imageIndex] = scene.meshInfo().modifications();

            } else {

                commandBuffersExecutor.updateUniformsOnly(meshViews.size(), this::updateMatricesData);
            }

            VulkanRenderer.get().submit(primaryCommandBuffer);

            this.camera = null;
            this.meshViews = null;
            inheritanceInfo = null;
            beginInfo = null;
        }
    }

    private void updateMatricesData(int index, VulkanThreadData threadData) {
        updateMatricesData(index, meshViews.get(index), camera, (VulkanPhongThreadData) threadData);
    }

    @Override
    public void beginCommandBuffer(VkCommandBuffer commandBuffer, VulkanPhongThreadData threadData) {

        vkCall(vkBeginCommandBuffer(commandBuffer, beginInfo));

        vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline.handle());
    }

    @Override
    public void recordCommandBuffer(int index, VkCommandBuffer commandBuffer, VulkanPhongThreadData threadData) {

        final MeshView meshView = meshViews.get(index);

        updateMatricesData(index, meshView, camera, threadData);

        for(Mesh mesh : meshView) {

            final VulkanVertexData vertexData = mesh.vertexData();
            final PhongMaterial material = mesh.material();

            if(threadData.lastVertexData != vertexData) {
                vertexData.bind(commandBuffer);
                threadData.lastVertexData = vertexData;
            }

            final MaterialDescriptorInfo materialInfo = materialDescriptorInfos.get(material);

            threadData.updateUniformInfo(index * MATRICES_UNIFORM_BUFFER_SIZE, materialInfo.dynamicOffset,
                    matricesDescriptorSet, materialInfo.descriptorSet, lightsDescriptorSet);

            threadData.bindDescriptorSets(commandBuffer, pipelineLayout.handle());

            if(vertexData.indexCount() > 0) {
                vkCmdDrawIndexed(commandBuffer, vertexData.indexCount(), 1, 0, 0, 0);
            } else {
                vkCmdDraw(commandBuffer, vertexData.vertexCount(), 1, 0, 0);
            }
        }
    }

    @Override
    public void endCommandBuffer(VkCommandBuffer commandBuffer, VulkanPhongThreadData threadData) {
        vkCall(vkEndCommandBuffer(commandBuffer));
    }

    private void beginPrimaryCommandBuffer(VkCommandBuffer commandBuffer) {

        try(MemoryStack stack = stackPush()) {

            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
                    // .flags(VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT);

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
                    .renderPass(swapchain.renderPass().handle())
                    .renderArea(VkRect2D.callocStack(stack)
                            .offset(VkOffset2D.callocStack(stack).set(0, 0))
                            .extent(swapchain.extent()))
                    .framebuffer(currentFramebuffer());

            VkClearValue.Buffer clearValues = VkClearValue.callocStack(2, stack);
            clearValues.get(0).color().float32(stack.floats(13/255.0f,	20/255.0f,	25/255.0f, 1.0f));
            clearValues.get(1).depthStencil().set(1.0f, 0);

            renderPassInfo.pClearValues(clearValues);

            vkCall(vkBeginCommandBuffer(commandBuffer, beginInfo));

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS);
        }
    }

    private void endPrimaryCommandBuffer(VkCommandBuffer commandBuffer) {
        vkCmdEndRenderPass(commandBuffer);
        vkCall(vkEndCommandBuffer(commandBuffer));
    }

    private void updateMatricesUniformBuffer(List<MeshView> meshViews) {

        if(matricesUniformBuffer.size() < meshViews.size() * MATRICES_UNIFORM_BUFFER_SIZE) {

            if(matricesUniformBufferData != MemoryUtil.NULL) {
                vmaFlushAllocation(allocator().handle(), matricesUniformBuffer.allocation(), 0, MATRICES_UNIFORM_BUFFER_SIZE);
                matricesUniformBuffer.unmapMemory();
                matricesUniformBufferData = MemoryUtil.NULL;
            }

            reallocateMatricesUniformBuffer(meshViews.size());

            matricesUniformBufferData = matricesUniformBuffer.mapMemory(0).get(0);
        }
    }

    private void updateMatricesData(int index, MeshView meshView, Camera camera, VulkanPhongThreadData threadData) {

        final long offset = index * MATRICES_UNIFORM_BUFFER_SIZE;

        ByteBuffer buffer = threadData.matricesData;

        projectionViewMatrix.mul(meshView.modelMatrix(), threadData.matrix).get(MATRICES_UNIFORM_BUFFER_MVP_OFFSET, buffer);
        meshView.modelMatrix().get(MATRICES_UNIFORM_BUFFER_MODEL_MATRIX_OFFSET, buffer);
        meshView.normalMatrix().get(MATRICES_UNIFORM_BUFFER_NORMAL_MATRIX_OFFSET, buffer);
        camera.transform().position().get(MATRICES_UNIFORM_BUFFER_CAMERA_POSITION_OFFSET, buffer);

        nmemcpy(matricesUniformBufferData + offset, memAddress(buffer), MATRICES_UNIFORM_BUFFER_SIZE);
    }

    private void reallocateMatricesUniformBuffer(int matricesCount) {

        final int newSize = matricesCount * MATRICES_UNIFORM_BUFFER_SIZE;

        matricesUniformBuffer.allocate(newSize);

        initMatricesDescriptorSets();
    }

    private void initMatricesDescriptorSets() {

        try(MemoryStack stack = stackPush()) {

            VkDescriptorBufferInfo.Buffer bufferInfos = VkDescriptorBufferInfo.callocStack(1, stack)
                    .buffer(matricesUniformBuffer.handle())
                    .offset(0)
                    .range(MATRICES_UNIFORM_BUFFER_SIZE);

            VkWriteDescriptorSet.Buffer writeDescriptorSets = VkWriteDescriptorSet.callocStack(matricesDescriptorPool.size(), stack);

            for(int i = 0; i < writeDescriptorSets.capacity(); i++) {
                writeDescriptorSets.get(i)
                        .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                        .dstBinding(0)
                        .dstArrayElement(0)
                        .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC)
                        .descriptorCount(1)
                        .dstSet(matricesDescriptorPool.descriptorSet(i))
                        .pBufferInfo(bufferInfos);
            }

            vkUpdateDescriptorSets(logicalDevice().handle(), writeDescriptorSets, null);
        }
    }

    private void updateMaterialInformation(Set<Material> materials, MemoryStack stack) {

        if(materials.stream()
                .filter(material -> material instanceof PhongMaterial)
                .allMatch(materialDescriptorInfos::containsKey)) {

            return;
        }

        List<PhongMaterial> newMaterials = materials.stream()
                .unordered()
                .filter(material -> material instanceof PhongMaterial)
                .map(PhongMaterial.class::cast)
                .filter(material -> !materialDescriptorInfos.containsKey(material))
                .collect(Collectors.toList());

        if(!newMaterials.isEmpty()) {
            createMaterialsDescriptorSets(newMaterials);
        }
    }

    private void createMaterialsDescriptorSets(List<PhongMaterial> newMaterials) {

        final long newSize = MATERIAL_UNIFORM_BUFFER_SIZE * newMaterials.size();

        VulkanMappableBuffer oldMaterialUniformBuffer = materialUniformBuffer;
        materialUniformBuffer = new VulkanUniformBuffer(newSize);

        VulkanMappableBuffer.copy(oldMaterialUniformBuffer, materialUniformBuffer, oldMaterialUniformBuffer.size());

        int descriptorSetIndex = materialDescriptorInfos.size();

        materialDescriptorPool.ensure(newMaterials.size());

        try(MemoryStack stack = stackPush()) {

            final ByteBuffer buffer = stack.malloc(MATERIAL_UNIFORM_BUFFER_SIZE);

            for(PhongMaterial material : newMaterials) {

                final int dynamicOffset = descriptorSetIndex * MATERIAL_UNIFORM_BUFFER_SIZE; // Check alignment
                final long descriptorSet = materialDescriptorPool.descriptorSet(descriptorSetIndex);

                initMaterialDescriptorSet(material, descriptorSet);

                materialDescriptorInfos.put(material, new MaterialDescriptorInfo(descriptorSet, dynamicOffset));

                materialUniformBuffer.update(descriptorSetIndex * MATERIAL_UNIFORM_BUFFER_SIZE, material.get(0, buffer));

                ++descriptorSetIndex;
            }
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


            vkUpdateDescriptorSets(logicalDevice().handle(), writeDescriptorSets, null);
        }
    }

    private void updateLightsUniformBuffer(SceneEnvironment environment, MemoryStack stack) {

        final long lightsUniformBufferData = this.lightsUniformBufferData;

        final DirectionalLight directionalLight = environment.directionalLight();
        final int pointLightsCount = environment.pointLightsCount();
        final int spotLightsCount = environment.spotLightsCount();

        ByteBuffer directionalLightBuffer = stack.calloc(Light.SIZEOF);

        if(directionalLight != null) {
            directionalLight.get(0, directionalLightBuffer);
        }

        nmemcpy(lightsUniformBufferData + DIRECTIONAL_LIGHT_OFFSET, memAddress(directionalLightBuffer), Light.SIZEOF);

        if(pointLightsCount > 0) {

            ByteBuffer buffer = stack.malloc(pointLightsCount * Light.SIZEOF);

            for(int i = 0;i < pointLightsCount;i++) {
                environment.pointLight(i).get(i * Light.SIZEOF, buffer);
            }

            nmemcpy(lightsUniformBufferData + POINT_LIGHTS_OFFSET, memAddress(buffer), buffer.limit());
        }

        if(spotLightsCount > 0) {

            ByteBuffer buffer = stack.malloc(spotLightsCount * Light.SIZEOF);

            for(int i = 0;i < spotLightsCount;i++) {
                environment.spotLight(i).get(i * Light.SIZEOF, buffer);
            }

            nmemcpy(lightsUniformBufferData + SPOT_LIGHTS_OFFSET, memAddress(buffer), buffer.limit());
        }

        ByteBuffer buffer = stack.malloc(Color.SIZEOF + INT32_SIZEOF * 2);

        environment.ambientColor().getRGBA(buffer).putInt(pointLightsCount).putInt(spotLightsCount);

        nmemcpy(lightsUniformBufferData + AMBIENT_COLOR_OFFSET, memAddress0(buffer), buffer.capacity());
    }

    private long currentFramebuffer() {
        return swapchain.renderPass().framebuffers().get(VulkanRenderer.get().currentSwapchainImageIndex());
    }

    private void createDescriptorSetLayouts() {

        matricesDescriptorSetLayout = new VulkanDescriptorSetLayout.Builder()
                .binding(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, 1, null, VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT) // Matrices
                .buildAndPop();

        materialDescriptorSetLayout = new VulkanDescriptorSetLayout.Builder()
                .binding(1, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, 1, null, VK_SHADER_STAGE_FRAGMENT_BIT) // Material
                .binding(2, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 1, null, VK_SHADER_STAGE_FRAGMENT_BIT) // AmbientMap
                .binding(3, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 1, null, VK_SHADER_STAGE_FRAGMENT_BIT) // DiffuseMap
                .binding(4, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 1, null, VK_SHADER_STAGE_FRAGMENT_BIT) // SpecularMap
                .binding(5, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 1, null, VK_SHADER_STAGE_FRAGMENT_BIT) // EmissiveMap
                .buildAndPop();

        lightsDescriptorSetLayout = new VulkanDescriptorSetLayout.Builder()
                .binding(6, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 1, null, VK_SHADER_STAGE_FRAGMENT_BIT) // Lights
                .buildAndPop();
    }

    private void initLightsDescriptorSet() {

        try(MemoryStack stack = stackPush()) {

            VkDescriptorBufferInfo.Buffer bufferInfos = VkDescriptorBufferInfo.callocStack(1, stack)
                    .buffer(lightsUniformBuffer.handle())
                    .offset(0)
                    .range(VK_WHOLE_SIZE);

            VkWriteDescriptorSet.Buffer writeDescriptorSets = VkWriteDescriptorSet.callocStack(1, stack)
                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstBinding(6)
                    .dstSet(lightsDescriptorSet)
                    .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                    .descriptorCount(1)
                    .pBufferInfo(bufferInfos);

            vkUpdateDescriptorSets(logicalDevice().handle(), writeDescriptorSets, null);
        }
    }

    private void createPipelineLayout() {
        pipelineLayout = new VulkanPipelineLayout.Builder()
                // .addPushConstantRange(VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT, 0, PUSH_CONSTANT_SIZE)
                .addDescriptorSetLayout(matricesDescriptorSetLayout)
                .addDescriptorSetLayout(materialDescriptorSetLayout)
                .addDescriptorSetLayout(lightsDescriptorSetLayout)
                .buildAndPop();
    }

    private void createGraphicsPipeline() {

        VulkanShaderModule vertexShaderModule = new VulkanShaderModule(VERTEX_SHADER_PATH, VERTEX_STAGE);
        VulkanShaderModule fragmentShaderModule = new VulkanShaderModule(FRAGMENT_SHADER_PATH, FRAGMENT_STAGE);

        graphicsPipeline = new VulkanGraphicsPipeline.Builder()
                .pipelineLayout(pipelineLayout.handle())
                .renderPass(swapchain.renderPass().handle())
                .subpass(RENDER_SUBPASS)
                .addShaderModules(vertexShaderModule, fragmentShaderModule)
                .vertexInputState(vertexInputBindingsStack(VERTEX_LAYOUT), vertexInputAttributesStack(VERTEX_LAYOUT))
                .inputAssemblyState(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST, false)
                .viewports(getViewports())
                .scissors(getScissors())
                // .addDynamicStates(VK_DYNAMIC_STATE_VIEWPORT, VK_DYNAMIC_STATE_SCISSOR)
                .rasterizationState(getRasterizationStage())
                .multisampleState(getMultisampleState())
                .depthStencilState(getDepthStencilState())
                .addColorBlendAttachment(false, getColorBlendFlags())
                .buildAndPop();

        vertexShaderModule.release();
        fragmentShaderModule.release();
    }

    private VkViewport.Buffer getViewports() {
        final VkExtent2D swapchainExtent = Graphics.vulkan().swapchain().extent();
        return VkViewport.callocStack(1)
                .x(0.0f)
                .y(swapchainExtent.height())
                .width(swapchainExtent.width())
                .height(-swapchainExtent.height())
                .minDepth(0.0f)
                .maxDepth(1.0f);
    }

    private VkRect2D.Buffer getScissors() {
        return VkRect2D.callocStack(1)
                .offset(VkOffset2D.callocStack().set(0, 0))
                .extent(Graphics.vulkan().swapchain().extent());
    }

    private int getColorBlendFlags() {
        return VK_COLOR_COMPONENT_R_BIT
                | VK_COLOR_COMPONENT_G_BIT
                | VK_COLOR_COMPONENT_B_BIT
                | VK_COLOR_COMPONENT_A_BIT;
    }

    private VkPipelineDepthStencilStateCreateInfo getDepthStencilState() {
        return VkPipelineDepthStencilStateCreateInfo.callocStack()
                .sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
                .depthTestEnable(true)
                .depthWriteEnable(true)
                .depthCompareOp(VK_COMPARE_OP_LESS)
                .depthBoundsTestEnable(false)
                .minDepthBounds(0.0f)
                .maxDepthBounds(1.0f)
                .stencilTestEnable(false);
    }

    private VkPipelineMultisampleStateCreateInfo getMultisampleState() {
        return VkPipelineMultisampleStateCreateInfo.callocStack()
                .sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
                .sampleShadingEnable(true)
                .minSampleShading(0.2f)
                .rasterizationSamples(1);
    }

    private VkPipelineRasterizationStateCreateInfo getRasterizationStage() {
        return VkPipelineRasterizationStateCreateInfo.callocStack()
                .sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
                .depthClampEnable(false)
                .rasterizerDiscardEnable(false)
                .polygonMode(VK_POLYGON_MODE_FILL)
                .lineWidth(1.0f)
                // .cullMode(VK_CULL_MODE_BACK_BIT)
                .frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE)
                .depthBiasEnable(false);
    }

    private void setupCommandBufferInfos(MemoryStack stack) {

        inheritanceInfo = VkCommandBufferInheritanceInfo.callocStack(stack)
                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO)
                .renderPass(swapchain.renderPass().handle())
                .subpass(RENDER_SUBPASS)
                .framebuffer(currentFramebuffer());

        beginInfo = VkCommandBufferBeginInfo.callocStack(stack)
                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                .pInheritanceInfo(inheritanceInfo)
                .flags(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);
    }

    private VulkanDescriptorPool createMatricesDescriptorPool(int count) {
        return new VulkanDescriptorPool(
                matricesDescriptorSetLayout,
                count,
                count,
                VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC);
    }

    private VulkanDescriptorPool createMaterialDescriptorPool(int count) {
        return new VulkanDescriptorPool(
                materialDescriptorSetLayout,
                count,
                count,
                VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC,
                VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    }

    private VulkanPhongThreadData createThreadData() {
        return new VulkanPhongThreadData();
    }

    private static final class MaterialDescriptorInfo {

        private final long descriptorSet;
        private final int dynamicOffset;

        public MaterialDescriptorInfo(long descriptorSet, int dynamicOffset) {
            this.descriptorSet = descriptorSet;
            this.dynamicOffset = dynamicOffset;
        }
    }

}
