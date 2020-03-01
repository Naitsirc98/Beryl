package naitsirc98.beryl.graphics.vulkan.pipelines;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.util.types.StackBuilder;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanPipelineLayout implements VulkanObject.Long {

    private long vkPipelineLayout;

    public VulkanPipelineLayout(VkPipelineLayoutCreateInfo pipelineLayoutCreateInfo) {
        vkPipelineLayout = createVkPipelineLayout(pipelineLayoutCreateInfo);
    }

    @Override
    public long handle() {
        return vkPipelineLayout;
    }

    @Override
    public void free() {
        vkDestroyPipelineLayout(Graphics.vulkan().logicalDevice().handle(), vkPipelineLayout, null);
        vkPipelineLayout = VK_NULL_HANDLE;
    }

    private long createVkPipelineLayout(VkPipelineLayoutCreateInfo createInfo) {

        try(MemoryStack stack = stackPush()) {

            LongBuffer pPipelineLayout = stack.mallocLong(1);

            vkCall(vkCreatePipelineLayout(Graphics.vulkan().logicalDevice().handle(), createInfo, null, pPipelineLayout));

            return pPipelineLayout.get(0);
        }
    }

    public static final class Builder extends StackBuilder<VulkanPipelineLayout> {

        private List<VulkanDescriptorSetLayout> descriptorSetLayouts;
        private List<VkPushConstantRange> pushConstantRanges;

        public Builder() {
            descriptorSetLayouts = new ArrayList<>(1);
            pushConstantRanges = new ArrayList<>(1);
        }

        public Builder addDescriptorSetLayout(VulkanDescriptorSetLayout descriptorSetLayout) {
            descriptorSetLayouts.add(descriptorSetLayout);
            return this;
        }

        public Builder addPushConstantRange(int stageFlags, int offset, int size) {
            return addPushConstantRange(VkPushConstantRange.callocStack().set(stageFlags, offset, size));
        }

        public Builder addPushConstantRange(VkPushConstantRange pushConstantRange) {
            pushConstantRanges.add(pushConstantRange);
            return this;
        }

        @Override
        public VulkanPipelineLayout build() {
            return new VulkanPipelineLayout(VkPipelineLayoutCreateInfo.callocStack()
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
                    .pSetLayouts(getDescriptorSetLayouts())
                    .pPushConstantRanges(getPushConstantRanges()));
        }

        @Override
        public void pop() {
            super.pop();
            descriptorSetLayouts = null;
            pushConstantRanges = null;
        }

        private LongBuffer getDescriptorSetLayouts() {

            LongBuffer descriptorSetLayouts = stackMallocLong(this.descriptorSetLayouts.size());

            for(int i = 0;i < descriptorSetLayouts.capacity();i++) {
                descriptorSetLayouts.put(i, this.descriptorSetLayouts.get(i).handle());
            }

            return descriptorSetLayouts;
        }

        private VkPushConstantRange.Buffer getPushConstantRanges() {

            VkPushConstantRange.Buffer pushConstantRanges = VkPushConstantRange.callocStack(this.pushConstantRanges.size());

            for(int i = 0;i < pushConstantRanges.capacity();i++) {
                pushConstantRanges.get(i).set(this.pushConstantRanges.get(i));
            }

            return pushConstantRanges;
        }
    }

}
