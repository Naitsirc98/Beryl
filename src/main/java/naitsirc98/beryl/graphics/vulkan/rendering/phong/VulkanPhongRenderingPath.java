package naitsirc98.beryl.graphics.vulkan.rendering.phong;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.graphics.vulkan.buffers.VulkanUniformBuffer;
import naitsirc98.beryl.graphics.vulkan.commands.VulkanCommandBufferRecorder;
import naitsirc98.beryl.graphics.vulkan.commands.VulkanCommandBufferThreadExecutor;
import naitsirc98.beryl.graphics.vulkan.descriptors.VulkanDescriptorPool;
import naitsirc98.beryl.graphics.vulkan.descriptors.VulkanDescriptorSetLayout;
import naitsirc98.beryl.graphics.vulkan.pipelines.VulkanGraphicsPipeline;
import naitsirc98.beryl.graphics.vulkan.pipelines.VulkanPipelineLayout;
import naitsirc98.beryl.graphics.vulkan.pipelines.VulkanShaderModule;
import naitsirc98.beryl.graphics.vulkan.rendering.VulkanRenderer;
import naitsirc98.beryl.graphics.vulkan.swapchain.VulkanSwapchain;
import naitsirc98.beryl.graphics.vulkan.swapchain.VulkanSwapchainDependent;
import naitsirc98.beryl.graphics.vulkan.vertex.VulkanVertexData;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.lights.Light;
import naitsirc98.beryl.lights.SpotLight;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import naitsirc98.beryl.resources.Resources;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.camera.Camera;
import naitsirc98.beryl.scenes.components.lights.LightSource;
import naitsirc98.beryl.scenes.components.meshes.MeshView;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.graphics.vulkan.rendering.phong.VulkanPhongThreadData.*;
import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static naitsirc98.beryl.graphics.vulkan.vertex.VulkanVertexInputUtils.vertexInputAttributesStack;
import static naitsirc98.beryl.graphics.vulkan.vertex.VulkanVertexInputUtils.vertexInputBindingsStack;
import static naitsirc98.beryl.util.handles.LongHandle.NULL;
import static naitsirc98.beryl.util.types.ByteSizeUtils.sizeof;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.INT32_SIZEOF;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.libc.LibCString.nmemcpy;
import static org.lwjgl.vulkan.VK10.*;

public final class VulkanPhongRenderingPath extends RenderingPath
        implements VulkanObject, VulkanCommandBufferRecorder<VulkanPhongThreadData>, VulkanSwapchainDependent {

    public static final VertexLayout VERTEX_LAYOUT = VertexLayout.VERTEX_LAYOUT_3D;

    private static final int RENDER_SUBPASS = 0;

    private static final Path VERTEX_SHADER_PATH;
    private static final Path FRAGMENT_SHADER_PATH;

    private static final int LIGHTS_UNIFORM_BUFFER_SIZE = 4 * 256 * FLOAT32_SIZEOF;

    static {

        Path vertexPath = null;
        Path fragmentPath = null;

        try {
            vertexPath = Resources.getPath("shaders/vk/phong/phong.vk.vert");
            fragmentPath = Resources.getPath("shaders/vk/phong/phong.vk.frag");
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
    private VulkanDescriptorPool lightsDescriptorPool;
    private long lightsDescriptorSet;
    private VulkanSwapchain swapchain;
    private VulkanCommandBufferThreadExecutor<VulkanPhongThreadData> commandBuilderExecutor;

    private VulkanUniformBuffer lightsUniformBuffer;
    private long lightsUniformBufferData;

    private Matrix4f projectionViewMatrix;
    private List<MeshView> meshViews;
    private Set<Material> materials;
    private Camera camera;

    private VkCommandBufferInheritanceInfo inheritanceInfo;
    private VkCommandBufferBeginInfo beginInfo;

    private VulkanPhongRenderingPath() {

    }

    @Override
    protected void init() {
        swapchain = Graphics.vulkan().swapchain();
        swapchain.addSwapchainDependent(this);
        createDescriptorSetLayouts();
        lightsUniformBuffer = new VulkanUniformBuffer(LIGHTS_UNIFORM_BUFFER_SIZE);
        lightsUniformBufferData = lightsUniformBuffer.mapMemory(0).get(0);
        lightsDescriptorPool = new VulkanDescriptorPool(lightsDescriptorSetLayout, 1, 1, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
        lightsDescriptorSet = lightsDescriptorPool.descriptorSet(0);
        initLightsDescriptorSet();
        createPipelineLayout();
        createGraphicsPipeline();
        commandBuilderExecutor = new VulkanCommandBufferThreadExecutor<>(this::createThreadData);
        projectionViewMatrix = new Matrix4f();
    }

    @Override
    protected void terminate() {
        free();
    }

    @Override
    public void free() {

        if(lightsUniformBufferData != NULL) {
            lightsUniformBuffer.unmapMemory();
            lightsUniformBufferData = NULL;
        }

        commandBuilderExecutor.free();
        pipelineLayout.free();
        graphicsPipeline.free();
        lightsDescriptorPool.free();
        lightsUniformBuffer.free();
    }

    @Override
    public void onSwapchainRecreate() {

        pipelineLayout.free();
        graphicsPipeline.free();

        createPipelineLayout();
        createGraphicsPipeline();
    }

    @Override
    public void render(Camera camera, Scene scene) {

        final List<MeshView> meshViews = scene.meshViews();

        if(meshViews.size() == 0) {
            return;
        }

        this.camera = camera;
        this.meshViews = meshViews;
        this.materials = scene.materials();

        projectionViewMatrix.set(camera.projectionViewMatrix());

        try(MemoryStack stack = stackPush()) {

            updateLightsUniformBuffer(scene.lightSources());

            setupCommandBufferInfos(stack);

            VkCommandBuffer primaryCommandBuffer = VulkanRenderer.get().currentCommandBuffer();

            beginPrimaryCommandBuffer(primaryCommandBuffer);

            commandBuilderExecutor.recordCommandBuffers(meshViews.size(), primaryCommandBuffer, this);

            endPrimaryCommandBuffer(primaryCommandBuffer);

            this.camera = null;
            this.meshViews = null;
            inheritanceInfo = null;
            beginInfo = null;
        }
    }

    private void updateLightsUniformBuffer(List<LightSource> lightSources) {

        if(lightSources.isEmpty()) {
            return;
        }

        int offset = FLOAT32_SIZEOF;

        try(MemoryStack stack = stackPush()) {

            nmemcpy(lightsUniformBufferData, memAddress(stack.malloc(FLOAT32_SIZEOF).putFloat(0, lightSources.size())), FLOAT32_SIZEOF);

            ByteBuffer buffer = stack.malloc(SpotLight.SIZEOF + FLOAT32_SIZEOF);

            for(LightSource lightSource : lightSources) {

                final Light<?> light = lightSource.light();

                buffer.putFloat(light.type());
                light.get(buffer).flip();

                nmemcpy(lightsUniformBufferData + offset, memAddress0(buffer), buffer.limit());

                offset += buffer.limit();

                buffer.limit(buffer.capacity());
            }
        }
    }

    @Override
    public void beginCommandBuffer(VkCommandBuffer commandBuffer, VulkanPhongThreadData threadData) {

        vkCall(vkBeginCommandBuffer(commandBuffer, beginInfo));

        vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline.handle());

        threadData.begin(meshViews, materials);
    }

    @Override
    public void recordCommandBuffer(int index, VkCommandBuffer commandBuffer, VulkanPhongThreadData threadData) {

        final MeshView meshView = meshViews.get(index);

        updatePushConstants(commandBuffer, meshView, threadData);

        final VulkanVertexData vertexData = meshView.mesh().vertexData();

        if(threadData.lastVertexData != vertexData) {
            vertexData.bind(commandBuffer);
            threadData.lastVertexData = vertexData;
        }

        threadData.updateUniformData(index, meshView, lightsDescriptorSet);

        threadData.bindDescriptorSets(commandBuffer, pipelineLayout.handle());

        if (vertexData.indexCount() == 0) {
            vkCmdDraw(commandBuffer, vertexData.vertexCount(), 1, 0, 0);
        } else {
            vkCmdDrawIndexed(commandBuffer, vertexData.indexCount(), 1, 0, 0, 0);
        }
    }

    private void updatePushConstants(VkCommandBuffer commandBuffer, MeshView meshView, VulkanPhongThreadData threadData) {

        final ByteBuffer pushConstantData = threadData.pushConstantData;

        projectionViewMatrix.mul(meshView.modelMatrix(), threadData.matrix).get(0, pushConstantData);
        camera.transform().position().get(MVP_PUSH_CONSTANT_SIZE, pushConstantData);

        nvkCmdPushConstants(
                commandBuffer,
                pipelineLayout.handle(),
                VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT,
                0,
                PUSH_CONSTANT_SIZE,
                threadData.pushConstantDataAddress);
    }

    @Override
    public void endCommandBuffer(VkCommandBuffer commandBuffer, VulkanPhongThreadData threadData) {
        vkCall(vkEndCommandBuffer(commandBuffer));
    }

    private void beginPrimaryCommandBuffer(VkCommandBuffer commandBuffer) {

        try(MemoryStack stack = stackPush()) {

            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
                    .renderPass(swapchain.renderPass().handle())
                    .renderArea(VkRect2D.callocStack(stack)
                            .offset(VkOffset2D.callocStack(stack).set(0, 0))
                            .extent(swapchain.extent()))
                    .framebuffer(currentFramebuffer());

            VkClearValue.Buffer clearValues = VkClearValue.callocStack(2, stack);
            clearValues.get(0).color().float32(stack.floats(0.1f, 0.1f, 0.1f, 1.0f));
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

    private long currentFramebuffer() {
        return swapchain.renderPass().framebuffers().get(VulkanRenderer.get().currentSwapchainImageIndex());
    }

    private void createDescriptorSetLayouts() {

        matricesDescriptorSetLayout = new VulkanDescriptorSetLayout.Builder()
                .binding(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, 1, null, VK_SHADER_STAGE_VERTEX_BIT) // Matrices
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
                .addPushConstantRange(VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT, 0, PUSH_CONSTANT_SIZE)
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

        vertexShaderModule.free();
        fragmentShaderModule.free();
    }

    private VkViewport.Buffer getViewports() {
        final VkExtent2D swapchainExtent = Graphics.vulkan().swapchain().extent();
        return VkViewport.callocStack(1)
                .x(0.0f)
                .y(0.0f)
                .width(swapchainExtent.width())
                .height(swapchainExtent.height())
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
                .cullMode(VK_CULL_MODE_BACK_BIT)
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

    private VulkanPhongThreadData createThreadData() {
        return new VulkanPhongThreadData(matricesDescriptorSetLayout, materialDescriptorSetLayout);
    }

}
