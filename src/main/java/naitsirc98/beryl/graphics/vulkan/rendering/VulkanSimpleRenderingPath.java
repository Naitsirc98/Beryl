package naitsirc98.beryl.graphics.vulkan.rendering;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.graphics.vulkan.pipelines.VulkanGraphicsPipeline;
import naitsirc98.beryl.graphics.vulkan.pipelines.VulkanPipelineLayout;
import naitsirc98.beryl.graphics.vulkan.pipelines.VulkanShaderModule;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import naitsirc98.beryl.resources.Resources;
import naitsirc98.beryl.scenes.components.camera.Camera;
import naitsirc98.beryl.scenes.components.meshes.MeshView;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;

import java.nio.file.Path;
import java.util.List;

import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.graphics.vulkan.vertex.VulkanVertexInputUtils.vertexInputAttributesStack;
import static naitsirc98.beryl.graphics.vulkan.vertex.VulkanVertexInputUtils.vertexInputBindingsStack;
import static naitsirc98.beryl.util.types.DataType.FLOAT32;
import static org.lwjgl.vulkan.VK10.*;

public final class VulkanSimpleRenderingPath extends RenderingPath {

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

    private VulkanSimpleRenderingPath() {

    }

    @Override
    protected void init() {
        createPipelineLayout();
        createGraphicsPipeline();
    }

    @Override
    public void render(Camera camera, List<MeshView> meshViews) {

    }

    @Override
    protected void terminate() {
        pipelineLayout.free();
        graphicsPipeline.free();
    }

    private void createPipelineLayout() {
        pipelineLayout = new VulkanPipelineLayout.Builder()
                .addPushConstantRange(VK_SHADER_STAGE_VERTEX_BIT, 0, PUSH_CONSTANT_SIZE)
                .buildAndPop();
    }

    private void createGraphicsPipeline() {

        final long swapchainRenderPass = Graphics.vulkan().swapchain().renderPass().handle();

        VulkanShaderModule vertexShaderModule = new VulkanShaderModule(VERTEX_SHADER_PATH, VERTEX_STAGE);
        VulkanShaderModule fragmentShaderModule = new VulkanShaderModule(FRAGMENT_SHADER_PATH, FRAGMENT_STAGE);

        graphicsPipeline = new VulkanGraphicsPipeline.Builder(pipelineLayout.handle(), swapchainRenderPass, RENDER_SUBPASS)
                .addShaderModules(vertexShaderModule, fragmentShaderModule)
                .vertexInputState(vertexInputBindingsStack(VERTEX_LAYOUT), vertexInputAttributesStack(VERTEX_LAYOUT))
                .inputAssemblyState(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST, false)
                .addDynamicStates(VK_DYNAMIC_STATE_VIEWPORT, VK_DYNAMIC_STATE_SCISSOR)
                .rasterizationState(getRasterizationStage())
                .multisampleState(getMultisampleState())
                .depthStencilState(getDepthStencilState())
                .addColorBlendAttachment(false, getColorBlendFlags())
                .buildAndPop();

        vertexShaderModule.free();
        fragmentShaderModule.free();
    }

    private int getColorBlendFlags() {
        return    VK_COLOR_COMPONENT_R_BIT
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
