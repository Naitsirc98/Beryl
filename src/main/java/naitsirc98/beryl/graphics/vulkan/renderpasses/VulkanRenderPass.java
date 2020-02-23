package naitsirc98.beryl.graphics.vulkan.renderpasses;

import naitsirc98.beryl.graphics.vulkan.devices.VulkanLogicalDevice;
import naitsirc98.beryl.util.Destructor;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;

import java.nio.LongBuffer;
import java.util.function.Function;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

@Destructor
public class VulkanRenderPass implements NativeResource {

    private final long vkRenderPass;
    private final VulkanLogicalDevice logicalDevice;
    private final VkSubpassDescription.Buffer subPasses;
    private final VulkanSubPassAttachmentDescriptions subPassAttachmentDescriptions;
    private final VkSubpassDependency.Buffer subpassDependencies;
    private long[] framebuffers;

    public VulkanRenderPass(VulkanLogicalDevice logicalDevice,
                            VkSubpassDescription.Buffer subPasses,
                            VulkanSubPassAttachmentDescriptions subPassAttachmentDescriptions,
                            VkSubpassDependency.Buffer subpassDependencies) {

        this.logicalDevice = logicalDevice;
        this.subPasses = subPasses;
        this.subPassAttachmentDescriptions = subPassAttachmentDescriptions;
        this.subpassDependencies = subpassDependencies;
        vkRenderPass = createVkRenderPass();
    }

    @Override
    public void free() {
        for(long framebuffer : framebuffers) {
            vkDestroyFramebuffer(logicalDevice.vkDevice(), framebuffer, null);
        }

        vkDestroyRenderPass(logicalDevice.vkDevice(), vkRenderPass, null);
    }

    public long vkRenderPass() {
        return vkRenderPass;
    }

    public VkSubpassDescription.Buffer subpasses() {
        return subPasses;
    }

    public VulkanSubPassAttachmentDescriptions subPassAttachmentDescriptions() {
        return subPassAttachmentDescriptions;
    }

    public VkSubpassDependency.Buffer subpassDependencies() {
        return subpassDependencies;
    }

    public long[] framebuffers() {
        return framebuffers;
    }

    public void createFramebuffers(int width, int height, int count, Function<Integer, LongBuffer> framebufferAttachments) {

        try(MemoryStack stack = stackPush()) {

            framebuffers = new long[count];

            LongBuffer pFramebuffer = stack.mallocLong(1);

            // Lets allocate the create info struct once and just update the pAttachments field each iteration
            VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.callocStack(stack);
            framebufferInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
            framebufferInfo.renderPass(vkRenderPass);
            framebufferInfo.width(width);
            framebufferInfo.height(height);
            framebufferInfo.layers(1);

            for(int i = 0;i < count;i++) {

                framebufferInfo.pAttachments(framebufferAttachments.apply(i));

                vkCall(vkCreateFramebuffer(logicalDevice.vkDevice(), framebufferInfo, null, pFramebuffer));

                framebuffers[i] = pFramebuffer.get(0);
            }
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

            vkCall(vkCreateRenderPass(logicalDevice.vkDevice(), renderPassInfo, null, pRenderPass));

            return pRenderPass.get(0);
        }
    }

}
