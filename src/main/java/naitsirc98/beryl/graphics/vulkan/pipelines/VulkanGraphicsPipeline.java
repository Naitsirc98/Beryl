package naitsirc98.beryl.graphics.vulkan.pipelines;

import naitsirc98.beryl.graphics.Graphics;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;

import java.nio.LongBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanGraphicsPipeline implements NativeResource {

    private long vkGraphicsPipeline;

    public VulkanGraphicsPipeline(VkGraphicsPipelineCreateInfo.Buffer createInfo) {
        vkGraphicsPipeline = createVkGraphicsPipeline(createInfo);
    }

    public long vkGraphicsPipeline() {
        return vkGraphicsPipeline;
    }

    @Override
    public void free() {
        vkDestroyPipeline(Graphics.vulkan().vkLogicalDevice(), vkGraphicsPipeline, null);
        vkGraphicsPipeline = VK_NULL_HANDLE;
    }

    private long createVkGraphicsPipeline(VkGraphicsPipelineCreateInfo.Buffer createInfo) {

        try(MemoryStack stack = stackPush()) {

            LongBuffer pGraphicsPipeline = stack.mallocLong(1);

            vkCall(vkCreateGraphicsPipelines(Graphics.vulkan().vkLogicalDevice(),
                    VK_NULL_HANDLE, createInfo, null, pGraphicsPipeline));

            return pGraphicsPipeline.get(0);
        }
    }
}
