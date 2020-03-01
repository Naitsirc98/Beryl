package naitsirc98.beryl.graphics.vulkan.pipelines;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;

import java.nio.LongBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
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
}
