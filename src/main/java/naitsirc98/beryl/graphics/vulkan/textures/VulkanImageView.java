package naitsirc98.beryl.graphics.vulkan.textures;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.logging.Log;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkComponentMapping;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

import java.nio.LongBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanImageView implements VulkanObject.Long {

    private long vkImageView;
    private VkImageViewCreateInfo info;

    public VulkanImageView() {
    }

    public VulkanImageView(VkImageViewCreateInfo createInfo) {
        init(createInfo);
    }

    public void init(VkImageViewCreateInfo createInfo) {

        if(createInfo.image() == VK_NULL_HANDLE) {
            Log.fatal("ImageViewCreateInfo.image is VK_NULL_HANDLE");
        }

        release();

        try(MemoryStack stack = stackPush()) {

            this.info = createInfo;

            LongBuffer pImageView = stack.mallocLong(1);

            vkCall(vkCreateImageView(logicalDevice().handle(), createInfo, null, pImageView));

            vkImageView = pImageView.get(0);
        }
    }

    @Override
    public long handle() {
        return vkImageView;
    }

    public long image() {
        return info.image();
    }

    public int type() {
        return info.viewType();
    }

    public int format() {
        return info.format();
    }

    public VkImageSubresourceRange subresourceRange() {
        return VkImageSubresourceRange.create().set(info.subresourceRange());
    }

    public VkComponentMapping components() {
        return VkComponentMapping.create().set(info.components());
    }

    public int flags() {
        return info.flags();
    }

    @Override
    public void release() {

        if(vkImageView == VK_NULL_HANDLE) {
            return;
        }

        vkDestroyImageView(logicalDevice().handle(), vkImageView, null);
        info.free();

        vkImageView = VK_NULL_HANDLE;
        info = null;
    }
}
