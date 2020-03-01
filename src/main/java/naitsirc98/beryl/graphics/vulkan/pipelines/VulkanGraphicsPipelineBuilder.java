package naitsirc98.beryl.graphics.vulkan.pipelines;

import naitsirc98.beryl.graphics.ShaderStage;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.vulkan.VK10.*;

public final class VulkanGraphicsPipelineBuilder implements NativeResource {

    private final long vkPipelineLayout;
    private final long vkRenderPass;
    private final int vkSubpass;

    private List<VulkanShaderModule> shaderModules;
    private VkPipelineVertexInputStateCreateInfo vertexInputState;
    private VkPipelineInputAssemblyStateCreateInfo inputAssemblyState;
    private List<Integer> dynamicStates;
    private VkPipelineRasterizationStateCreateInfo rasterizationState;
    private VkPipelineMultisampleStateCreateInfo multisampleState;
    private VkPipelineDepthStencilStateCreateInfo depthStencilState;
    private List<VkPipelineColorBlendAttachmentState> colorBlendAttachments;

    public VulkanGraphicsPipelineBuilder(long vkPipelineLayout, long vkRenderPass, int vkSubpass) {

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

    public VulkanGraphicsPipelineBuilder addColorBlendAttachment(boolean enable, int colorWriteMask) {
        return addColorBlendAttachment(VkPipelineColorBlendAttachmentState.callocStack()
                .blendEnable(enable)
                .colorWriteMask(colorWriteMask));
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
                .pColorBlendState(getColorBlendState())
                .layout(vkPipelineLayout)
                .renderPass(vkRenderPass)
                .subpass(vkSubpass)
                .basePipelineHandle(VK_NULL_HANDLE)
                .basePipelineIndex(-1));
    }

    private VkPipelineColorBlendStateCreateInfo getColorBlendState() {
        // TODO: make this configurable too
        return VkPipelineColorBlendStateCreateInfo.callocStack()
                   .sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
                   .logicOpEnable(false)
                   .logicOp(VK_LOGIC_OP_COPY)
                   .pAttachments(getColorBlendAttachments())
                   .blendConstants(stackGet().floats(0.0f, 0.0f, 0.0f, 0.0f));
    }

    private VkPipelineColorBlendAttachmentState.Buffer getColorBlendAttachments() {

        VkPipelineColorBlendAttachmentState.Buffer attachments =
                VkPipelineColorBlendAttachmentState.callocStack(colorBlendAttachments.size());

        for(int i = 0;i < attachments.capacity();i++) {
            attachments.get(i).set(colorBlendAttachments.get(i));
        }

        return attachments;
    }

    private VkPipelineShaderStageCreateInfo.Buffer getShaderStages() {

        VkPipelineShaderStageCreateInfo.Buffer shaderStages =
                VkPipelineShaderStageCreateInfo.callocStack(shaderModules.size());

        ByteBuffer entryPoint = stackGet().UTF8(VulkanShaderModule.DEFAULT_ENTRY_POINT);

        for(int i = 0;i < shaderStages.capacity();i++) {
            VulkanShaderModule shaderModule = shaderModules.get(i);
            shaderStages.get(i)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                    .module(shaderModule.handle())
                    .stage(shaderModule.stage().handle())
                    .pName(entryPoint);
        }

        return shaderStages;
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
