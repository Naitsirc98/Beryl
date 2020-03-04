package naitsirc98.beryl.graphics.vulkan.rendering;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.graphics.vulkan.commands.VulkanCommandBufferRecorder;
import naitsirc98.beryl.graphics.vulkan.commands.VulkanCommandBuilderExecutor;
import naitsirc98.beryl.graphics.vulkan.pipelines.VulkanGraphicsPipeline;
import naitsirc98.beryl.graphics.vulkan.pipelines.VulkanPipelineLayout;
import naitsirc98.beryl.graphics.vulkan.pipelines.VulkanShaderModule;
import naitsirc98.beryl.graphics.vulkan.swapchain.VulkanSwapchain;
import naitsirc98.beryl.graphics.vulkan.vertex.VulkanVertexData;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import naitsirc98.beryl.resources.Resources;
import naitsirc98.beryl.scenes.components.camera.Camera;
import naitsirc98.beryl.scenes.components.meshes.MeshView;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;

import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static naitsirc98.beryl.graphics.vulkan.vertex.VulkanVertexInputUtils.vertexInputAttributesStack;
import static naitsirc98.beryl.graphics.vulkan.vertex.VulkanVertexInputUtils.vertexInputBindingsStack;
import static naitsirc98.beryl.util.types.DataType.FLOAT32;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.vulkan.VK10.*;

public final class VulkanSimpleRenderingPath extends RenderingPath implements VulkanCommandBufferRecorder {

    public static final VertexLayout VERTEX_LAYOUT = VertexLayout.VERTEX_LAYOUT_3D;

    private static final int PUSH_CONSTANT_SIZE = 16 * FLOAT32.sizeof();
    private static final int RENDER_SUBPASS = 0;

    private static final Path VERTEX_SHADER_PATH;
    private static final Path FRAGMENT_SHADER_PATH;

    static {

        Path vertexPath = null;
        Path fragmentPath = null;

        try {
            vertexPath = Resources.getPath("shaders/simple/simple.vert");
            fragmentPath = Resources.getPath("shaders/simple/simple.frag");
        } catch (Exception e) {
            Log.fatal("Failed to get shader files for RenderingPath", e);
        }

        VERTEX_SHADER_PATH = vertexPath;
        FRAGMENT_SHADER_PATH = fragmentPath;
    }

    private VulkanPipelineLayout pipelineLayout;
    private VulkanGraphicsPipeline graphicsPipeline;
    private VulkanRenderer renderer;
    private VulkanSwapchain swapchain;
    private VulkanCommandBuilderExecutor commandBuilderExecutor;
    private Matrix4f projectionViewMatrix;
    private List<MeshView> meshViews;

    private VulkanSimpleRenderingPath() {

    }

    @Override
    protected void init() {
        swapchain = Graphics.vulkan().swapchain();
        createPipelineLayout();
        createGraphicsPipeline();
        renderer = Graphics.vulkan().renderer();
        commandBuilderExecutor = new VulkanCommandBuilderExecutor();
        projectionViewMatrix = new Matrix4f();
    }

    @Override
    public void render(Camera camera, List<MeshView> meshViews) {

        this.meshViews = meshViews;

        projectionViewMatrix = projectionViewMatrix.set(camera.projectionMatrix());
        projectionViewMatrix.m11(-projectionViewMatrix.m11());
        projectionViewMatrix.mul(camera.viewMatrix());

        VkCommandBuffer primaryCommandBuffer = renderer.currentCommandBuffer();

        beginPrimaryCommandBuffer(primaryCommandBuffer);

        commandBuilderExecutor.recordCommandBuffers(meshViews.size(), primaryCommandBuffer, this);

        endPrimaryCommandBuffer(primaryCommandBuffer);

        this.meshViews = null;
    }


    @Override
    public void beginCommandBuffer(VkCommandBuffer commandBuffer) {

        try (MemoryStack stack = stackPush()) {

            VkCommandBufferInheritanceInfo inheritanceInfo = VkCommandBufferInheritanceInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO)
                    .renderPass(swapchain.renderPass().handle())
                    .subpass(0)
                    .framebuffer(currentFramebuffer());

            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                    .pInheritanceInfo(inheritanceInfo)
                    .flags(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);


            vkCall(vkBeginCommandBuffer(commandBuffer, beginInfo));

            vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline.handle());
        }
    }

    @Override
    public void recordCommandBuffer(int index, VkCommandBuffer commandBuffer, ByteBuffer pushConstantData, Matrix4f mvp) {

        final MeshView meshView = meshViews.get(index);

        projectionViewMatrix.mul(meshView.modelMatrix(), mvp).get(pushConstantData);

        nvkCmdPushConstants(
                commandBuffer,
                pipelineLayout.handle(),
                VK_SHADER_STAGE_VERTEX_BIT,
                0,
                PUSH_CONSTANT_SIZE,
                memAddress(pushConstantData));

        VulkanVertexData vertexData = meshView.mesh().vertexData();

        vertexData.bind(commandBuffer);

        if (vertexData.indexCount() == 0) {
            vkCmdDraw(commandBuffer, vertexData.vertexCount(), 1, 0, 0);
        } else {
            vkCmdDrawIndexed(commandBuffer, vertexData.indexCount(), 1, 0, 0, 0);
        }
    }

    @Override
    public void endCommandBuffer(VkCommandBuffer commandBuffer) {
        vkCall(vkEndCommandBuffer(commandBuffer));
    }

    private void endPrimaryCommandBuffer(VkCommandBuffer commandBuffer) {
        vkCmdEndRenderPass(commandBuffer);
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

    private long currentFramebuffer() {
        return swapchain.renderPass().framebuffers().get(renderer.currentSwapchainImageIndex());
    }

    @Override
    protected void terminate() {
        commandBuilderExecutor.free();
        pipelineLayout.free();
        graphicsPipeline.free();
    }

    private void createPipelineLayout() {
        pipelineLayout = new VulkanPipelineLayout.Builder()
                .addPushConstantRange(VK_SHADER_STAGE_VERTEX_BIT, 0, PUSH_CONSTANT_SIZE)
                .buildAndPop();
    }

    private void createGraphicsPipeline() {

        VulkanShaderModule vertexShaderModule = new VulkanShaderModule(VERTEX_SHADER_PATH, VERTEX_STAGE);
        VulkanShaderModule fragmentShaderModule = new VulkanShaderModule(FRAGMENT_SHADER_PATH, FRAGMENT_STAGE);

        graphicsPipeline = new VulkanGraphicsPipeline.Builder()
                .pipelineLayout(pipelineLayout.handle())
                .renderPass(swapchain.renderPass().handle())
                .subpass(0)
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
}
