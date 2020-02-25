package naitsirc98.beryl.graphics.vulkan.renderpasses;

import org.lwjgl.vulkan.VkAttachmentDescription;

public class VulkanSubPassAttachments {

    private final VkAttachmentDescription.Buffer colorAttachments;
    private final VkAttachmentDescription.Buffer resolveAttachments;
    private final VkAttachmentDescription depthStencilAttachment;

    public VulkanSubPassAttachments(VkAttachmentDescription.Buffer colorAttachments,
                                    VkAttachmentDescription.Buffer resolveAttachments,
                                    VkAttachmentDescription depthStencilAttachment) {
        this.colorAttachments = colorAttachments;
        this.resolveAttachments = resolveAttachments;
        this.depthStencilAttachment = depthStencilAttachment;
    }

    public VkAttachmentDescription.Buffer colorAttachments() {
        return colorAttachments;
    }

    public VkAttachmentDescription.Buffer resolveAttachments() {
        return resolveAttachments;
    }

    public VkAttachmentDescription depthStencilAttachment() {
        return depthStencilAttachment;
    }

    public int size() {

        int size = 0;

        if(colorAttachments != null) {
            size += colorAttachments.capacity();
        }

        if(resolveAttachments != null) {
            size += resolveAttachments.capacity();
        }

        if(depthStencilAttachment != null) {
            ++size;
        }

        return size;
    }

    public VkAttachmentDescription.Buffer attachments() {

        VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.create(size());

        if(colorAttachments != null) {
            attachments.put(colorAttachments);
        }

        if(resolveAttachments != null) {
            attachments.put(resolveAttachments);
        }

        if(depthStencilAttachment != null) {
            attachments.put(depthStencilAttachment);
        }

        return attachments.rewind();
    }
}
