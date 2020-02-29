package naitsirc98.beryl.graphics.vulkan.pipelines;

import naitsirc98.beryl.graphics.ShaderStage;
import naitsirc98.beryl.graphics.shaders.SPIRVBytecode;
import naitsirc98.beryl.meshes.vertices.Vertex;
import naitsirc98.beryl.resources.Resources;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.graphics.shaders.SPIRVCompiler.compileShaderFile;
import static org.lwjgl.system.MemoryStack.stackPop;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public final class VulkanGraphicsPipelineBuilder implements NativeResource {

    private final long vkPipelineLayout;
    private final long vkRenderPass;
    private final long vkSubpass;

    private List<VulkanShaderModule> shaderModules;
    private VkPipelineVertexInputStateCreateInfo vertexInputState;
    private VkPipelineInputAssemblyStateCreateInfo inputAssemblyState;
    private List<Integer> dynamicStates;
    private VkPipelineRasterizationStateCreateInfo rasterizationState;
    private VkPipelineMultisampleStateCreateInfo multisampleState;
    private VkPipelineDepthStencilStateCreateInfo depthStencilState;
    private List<VkPipelineColorBlendAttachmentState> colorBlendAttachments;

    public VulkanGraphicsPipelineBuilder(long vkPipelineLayout, long vkRenderPass, long vkSubpass) {

        stackPush();

        this.vkPipelineLayout = vkPipelineLayout;
        this.vkRenderPass = vkRenderPass;
        this.vkSubpass = vkSubpass;

        shaderModules = new ArrayList<>(2);

        vertexInputState = VkPipelineVertexInputStateCreateInfo.callocStack()
                .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);

        inputAssemblyState = VkPipelineInputAssemblyStateCreateInfo.callocStack()
                .sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);

        dynamicStates = new ArrayList<>(2);

        rasterizationState = VkPipelineRasterizationStateCreateInfo.callocStack()
                .sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);

        multisampleState = VkPipelineMultisampleStateCreateInfo.callocStack()
                .sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);

        depthStencilState = VkPipelineDepthStencilStateCreateInfo.callocStack()
                .sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);

        colorBlendAttachments = new ArrayList<>(1);
    }

    public VulkanGraphicsPipelineBuilder addShaderModule(Path shaderFile, ShaderStage stage) {
        shaderModules.add(new VulkanShaderModule(shaderFile, stage));
        return this;
    }

    public VkPipelineVertexInputStateCreateInfo vertexInputState() {
        return vertexInputState;
    }

    public VkPipelineInputAssemblyStateCreateInfo inputAssemblyState() {
        return inputAssemblyState;
    }

    public List<Integer> dynamicStates() {
        return dynamicStates;
    }

    public VkPipelineRasterizationStateCreateInfo rasterizationState() {
        return rasterizationState;
    }

    public VkPipelineMultisampleStateCreateInfo multisampleState() {
        return multisampleState;
    }

    public VkPipelineDepthStencilStateCreateInfo depthStencilState() {
        return depthStencilState;
    }

    public VulkanGraphicsPipelineBuilder addColorBlendAttachment(boolean enable, int mask) {
        return addColorBlendAttachment(VkPipelineColorBlendAttachmentState.callocStack()
                .blendEnable(enable)
                .colorWriteMask(mask));
    }

    public VulkanGraphicsPipelineBuilder addColorBlendAttachment(VkPipelineColorBlendAttachmentState colorBlendAttachment) {
        colorBlendAttachments.add(colorBlendAttachment);
        return this;
    }

    public VulkanGraphicsPipeline build() {

        return new VulkanGraphicsPipeline(VkGraphicsPipelineCreateInfo.callocStack(1)
                .sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
                .pStages(getShaderStages())
                .pVertexInputState(vertexInputState)
                .pInputAssemblyState(inputAssemblyState)
                .pRasterizationState(rasterizationState)
                .pMultisampleState(multisampleState)
                .pDepthStencilState(depthStencilState)
                .pColorBlendState(getColorBlendAttachments())
                .layout(vkPipelineLayout)
                .renderPass(vkRenderPass)
                .subpass(vkSubpass)
                .basePipelineHandle(VK_NULL_HANDLE)
                .basePipelineIndex(-1));
    }

    private VkPipelineShaderStageCreateInfo.Buffer getShaderStages() {

        VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.callocStack(shaderModules.size());

        return shaderStages;
    }

    private void createGraphicsPipeline() {

        try(MemoryStack stack = stackPush()) {

            // Let's compile the GLSL shaders into SPIR-V at runtime using the shaderc library
            // Check ShaderSPIRVUtils class to see how it can be done
            SPIRVBytecode vertShaderSPIRV = compileShaderFile(Resources.getPath("shaders/26_shader_depth.vert"), VERTEX_STAGE);
            SPIRVBytecode fragShaderSPIRV = compileShaderFile(Resources.getPath("shaders/26_shader_depth.frag"), FRAGMENT_STAGE);

            long vertShaderModule = createShaderModule(vertShaderSPIRV.bytecode());
            long fragShaderModule = createShaderModule(fragShaderSPIRV.bytecode());

            ByteBuffer entryPoint = stack.UTF8("main");

            VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.callocStack(2, stack);

            VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(0);

            vertShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
            vertShaderStageInfo.stage(VK_SHADER_STAGE_VERTEX_BIT);
            vertShaderStageInfo.module(vertShaderModule);
            vertShaderStageInfo.pName(entryPoint);

            VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(1);

            fragShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
            fragShaderStageInfo.stage(VK_SHADER_STAGE_FRAGMENT_BIT);
            fragShaderStageInfo.module(fragShaderModule);
            fragShaderStageInfo.pName(entryPoint);

            // ===> VERTEX STAGE <===

            VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.callocStack(stack);
            vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
            vertexInputInfo.pVertexBindingDescriptions(Vertex.getBindingDescription());
            vertexInputInfo.pVertexAttributeDescriptions(Vertex.getAttributeDescriptions());

            // ===> ASSEMBLY STAGE <===

            VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.callocStack(stack);
            inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
            inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
            inputAssembly.primitiveRestartEnable(false);

            // ===> VIEWPORT & SCISSOR

            VkViewport.Buffer viewport = VkViewport.callocStack(1, stack);
            viewport.x(0.0f);
            viewport.y(0.0f);
            viewport.width(swapChainExtent.width());
            viewport.height(swapChainExtent.height());
            viewport.minDepth(0.0f);
            viewport.maxDepth(1.0f);

            VkRect2D.Buffer scissor = VkRect2D.callocStack(1, stack);
            scissor.offset(VkOffset2D.callocStack(stack).set(0, 0));
            scissor.extent(swapChainExtent);

            VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.callocStack(stack);
            viewportState.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
            viewportState.pViewports(viewport);
            viewportState.pScissors(scissor);

            // ===> RASTERIZATION STAGE <===

            VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.callocStack(stack);
            rasterizer.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
            rasterizer.depthClampEnable(false);
            rasterizer.rasterizerDiscardEnable(false);
            rasterizer.polygonMode(VK_POLYGON_MODE_FILL);
            rasterizer.lineWidth(1.0f);
            rasterizer.cullMode(VK_CULL_MODE_BACK_BIT);
            rasterizer.frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE);
            rasterizer.depthBiasEnable(false);

            // ===> MULTISAMPLING <===

            VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.callocStack(stack);
            multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
            multisampling.sampleShadingEnable(true);
            multisampling.minSampleShading(0.2f); // Enable sample shading in the pipeline
            multisampling.rasterizationSamples(1); // Min fraction for sample shading; closer to one is smoother

            VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.callocStack(stack);
            depthStencil.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
            depthStencil.depthTestEnable(true);
            depthStencil.depthWriteEnable(true);
            depthStencil.depthCompareOp(VK_COMPARE_OP_LESS);
            depthStencil.depthBoundsTestEnable(false);
            depthStencil.minDepthBounds(0.0f); // Optional
            depthStencil.maxDepthBounds(1.0f); // Optional
            depthStencil.stencilTestEnable(false);

            // ===> COLOR BLENDING <===

            VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.callocStack(1, stack);
            colorBlendAttachment.colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);
            colorBlendAttachment.blendEnable(false);

            VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.callocStack(stack);
            colorBlending.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
            colorBlending.logicOpEnable(false);
            colorBlending.logicOp(VK_LOGIC_OP_COPY);
            colorBlending.pAttachments(colorBlendAttachment);
            colorBlending.blendConstants(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));

            // ===> PIPELINE LAYOUT CREATION <===

            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
            pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
            // pipelineLayoutInfo.pSetLayouts(stack.longs(descriptorSetLayout));

            LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);

            if(vkCreatePipelineLayout(device, pipelineLayoutInfo, null, pPipelineLayout) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create pipeline layout");
            }

            pipelineLayout = pPipelineLayout.get(0);

            VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.callocStack(1, stack);
            pipelineInfo.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
            pipelineInfo.pStages(shaderStages);
            pipelineInfo.pVertexInputState(vertexInputInfo);
            pipelineInfo.pInputAssemblyState(inputAssembly);
            pipelineInfo.pViewportState(viewportState);
            pipelineInfo.pRasterizationState(rasterizer);
            pipelineInfo.pMultisampleState(multisampling);
            pipelineInfo.pDepthStencilState(depthStencil);
            pipelineInfo.pColorBlendState(colorBlending);
            pipelineInfo.layout(pipelineLayout);
            pipelineInfo.renderPass(renderPass);
            pipelineInfo.subpass(0);
            pipelineInfo.basePipelineHandle(VK_NULL_HANDLE);
            pipelineInfo.basePipelineIndex(-1);

            LongBuffer pGraphicsPipeline = stack.mallocLong(1);

            if(vkCreateGraphicsPipelines(device, VK_NULL_HANDLE, pipelineInfo, null, pGraphicsPipeline) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create graphics pipeline");
            }

            graphicsPipeline = pGraphicsPipeline.get(0);

            // ===> RELEASE RESOURCES <===

            vkDestroyShaderModule(device, vertShaderModule, null);
            vkDestroyShaderModule(device, fragShaderModule, null);

            vertShaderSPIRV.free();
            fragShaderSPIRV.free();
        }
    }

    @Override
    public void free() {

        stackPop();

        shaderModules = null;
        vertexInputState = null;
        inputAssemblyState = null;
        dynamicStates = null;
        rasterizationState = null;
        multisampleState = null;
        depthStencilState = null;
        colorBlendAttachments = null;
    }
}
