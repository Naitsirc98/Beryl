package naitsirc98.beryl.graphics.vulkan.pipelines;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.util.types.StackBuilder;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanGraphicsPipeline implements VulkanObject.Long {

    private long vkGraphicsPipeline;

    public VulkanGraphicsPipeline(VkGraphicsPipelineCreateInfo.Buffer createInfo) {
        vkGraphicsPipeline = createVkGraphicsPipeline(createInfo);
    }

    @Override
    public long handle() {
        return vkGraphicsPipeline;
    }

    @Override
    public void free() {
        vkDestroyPipeline(logicalDevice().handle(), vkGraphicsPipeline, null);
        vkGraphicsPipeline = VK_NULL_HANDLE;
    }

    private long createVkGraphicsPipeline(VkGraphicsPipelineCreateInfo.Buffer createInfo) {

        try(MemoryStack stack = stackPush()) {

            LongBuffer pGraphicsPipeline = stack.mallocLong(1);

            vkCall(vkCreateGraphicsPipelines(logicalDevice().handle(),
                    VK_NULL_HANDLE, createInfo, null, pGraphicsPipeline));

            return pGraphicsPipeline.get(0);
        }
    }

    public static final class Builder extends StackBuilder<VulkanGraphicsPipeline> {

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

        public Builder(long vkPipelineLayout, long vkRenderPass, int vkSubpass) {

            this.vkPipelineLayout = vkPipelineLayout;
            this.vkRenderPass = vkRenderPass;
            this.vkSubpass = vkSubpass;

            shaderModules = new ArrayList<>(2);

            vertexInputState = VkPipelineVertexInputStateCreateInfo.callocStack()
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);

            inputAssemblyState = VkPipelineInputAssemblyStateCreateInfo.callocStack()
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);

            dynamicStates = new ArrayList<>(2);

            colorBlendAttachments = new ArrayList<>(1);
        }

        public Builder addShaderModules(VulkanShaderModule... shaderModules) {
            this.shaderModules.addAll(Arrays.asList(shaderModules));
            return this;
        }

        public Builder addShaderModules(VulkanShaderModule shaderModule) {
            shaderModules.add(shaderModule);
            return this;
        }

        public Builder vertexInputState(VkVertexInputBindingDescription.Buffer bindingDescriptions,
                                        VkVertexInputAttributeDescription.Buffer attributeDescriptions) {

            vertexInputState.pVertexBindingDescriptions(bindingDescriptions)
                .pVertexAttributeDescriptions(attributeDescriptions);
            return this;
        }

        public Builder inputAssemblyState(int topology, boolean primRestartEnable) {
            inputAssemblyState.topology(topology)
                    .primitiveRestartEnable(primRestartEnable);
            return this;
        }

        public Builder addDynamicStates(int... dynamicStates) {
            for(int dynamicState : dynamicStates) {
                this.dynamicStates.add(dynamicState);
            }
            return this;
        }

        public Builder addDynamicStates(int dynamicState) {
            dynamicStates.add(dynamicState);
            return this;
        }

        public Builder rasterizationState(VkPipelineRasterizationStateCreateInfo rasterizationState) {
            this.rasterizationState = rasterizationState;
            return this;
        }

        public Builder multisampleState(VkPipelineMultisampleStateCreateInfo multisampleState) {
            this.multisampleState = multisampleState;
            return this;
        }

        public Builder depthStencilState(VkPipelineDepthStencilStateCreateInfo depthStencilState) {
            this.depthStencilState = depthStencilState;
            return this;
        }

        public Builder addColorBlendAttachment(boolean enable, int colorWriteMask) {
            return addColorBlendAttachment(VkPipelineColorBlendAttachmentState.callocStack()
                    .blendEnable(enable)
                    .colorWriteMask(colorWriteMask));
        }

        public Builder addColorBlendAttachment(VkPipelineColorBlendAttachmentState colorBlendAttachment) {
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
                    .pDynamicState(getPipelineDynamicStates())
                    .basePipelineHandle(VK_NULL_HANDLE)
                    .basePipelineIndex(-1));
        }

        private VkPipelineDynamicStateCreateInfo getPipelineDynamicStates() {
            return VkPipelineDynamicStateCreateInfo.callocStack()
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
                    .pDynamicStates(getDynamicStates());
        }

        private IntBuffer getDynamicStates() {

            IntBuffer dynamicStates = stackMallocInt(this.dynamicStates.size());

            for(int i = 0;i < dynamicStates.capacity();i++) {
                dynamicStates.put(i, this.dynamicStates.get(i));
            }

            return dynamicStates;
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
        public void pop() {

            super.pop();

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
}
