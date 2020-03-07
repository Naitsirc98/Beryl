package naitsirc98.beryl.graphics.vulkan.renderpasses;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;

import java.nio.LongBuffer;
import java.util.function.Function;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.*;


public class VulkanRenderPass implements VulkanObject.Long {

    private long vkRenderPass;
    private VkSubpassDescription.Buffer subPasses;
    private VulkanSubPassAttachments subPassAttachmentDescriptions;
    private VkSubpassDependency.Buffer subpassDependencies;
    private LongBuffer framebuffers;

    public VulkanRenderPass(VkSubpassDescription.Buffer subPasses,
                            VulkanSubPassAttachments subPassAttachmentDescriptions,
                            VkSubpassDependency.Buffer subpassDependencies) {

        this.subPasses = subPasses;
        this.subPassAttachmentDescriptions = subPassAttachmentDescriptions;
        this.subpassDependencies = subpassDependencies;
        vkRenderPass = createVkRenderPass();
    }

    @Override
    public long handle() {
        return vkRenderPass;
    }

    @Override
    public void free() {

        for(int i = 0;i < framebuffers.capacity();i++) {
            vkDestroyFramebuffer(logicalDevice().handle(), framebuffers.get(i), null);
        }
        memFree(framebuffers);

        vkDestroyRenderPass(logicalDevice().handle(), vkRenderPass, null);

        vkRenderPass = VK_NULL_HANDLE;
    }

    public VkSubpassDescription.Buffer subpasses() {
        return subPasses;
    }

    public VulkanSubPassAttachments subPassAttachmentDescriptions() {
        return subPassAttachmentDescriptions;
    }

    public VkSubpassDependency.Buffer subpassDependencies() {
        return subpassDependencies;
    }

    public LongBuffer framebuffers() {
        return framebuffers;
    }

    public void createFramebuffers(int width, int height, int count, Function<Integer, LongBuffer> framebufferAttachments) {

        try(MemoryStack stack = stackPush()) {

            framebuffers = memAllocLong(count);

            // Lets allocate the create info struct once and just update the pAttachments field each iteration
            VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.callocStack(stack);
            framebufferInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
            framebufferInfo.renderPass(vkRenderPass);
            framebufferInfo.width(width);
            framebufferInfo.height(height);
            framebufferInfo.layers(1);

            for(int i = 0;i < count;i++) {

                framebufferInfo.pAttachments(framebufferAttachments.apply(i));

                vkCall(vkCreateFramebuffer(logicalDevice().handle(), framebufferInfo, null, framebuffers.position(i)));
            }

            framebuffers.rewind();
        }
    }

    private long createVkRenderPass() {

        try(MemoryStack stack = stackPush()) {

            VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.callocStack(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
            renderPassInfo.pAttachments(subPassAttachmentDescriptions.attachments());
            renderPassInfo.pSubpasses(subPasses);
            renderPassInfo.pDependencies(subpassDependencies);

            LongBuffer pRenderPass = stack.mallocLong(1);

            vkCall(vkCreateRenderPass(logicalDevice().handle(), renderPassInfo, null, pRenderPass));

            return pRenderPass.get(0);
        }
    }

}
