package naitsirc98.beryl.graphics.vulkan.textures;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.util.types.IBuilder;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

import java.nio.IntBuffer;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.graphics.vulkan.util.VulkanFormatUtils.formatHasStencilComponent;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanRenderImage implements VulkanObject {

    private VulkanImage image;
    private VulkanImageView imageView;

    public VulkanRenderImage() {
    }

    public VulkanRenderImage(VulkanImage image, VulkanImageView imageView) {
        init(image, imageView);
    }

    public void init(VulkanImage image, VulkanImageView imageView) {
        this.image = image;
        this.imageView = imageView;
    }

    public VulkanImage image() {
        return image;
    }

    public VulkanImageView view() {
        return imageView;
    }

    public int width() {
        return image.width();
    }

    public int height() {
        return image.height();
    }

    public int depth() {
        return image.depth();
    }

    @Override
    public void free() {

        if(image != null) {
            image.free();
            image = null;
        }

        if(imageView != null) {
            imageView.free();
            imageView = null;
        }
    }


    public static final class Builder implements IBuilder<VulkanRenderImage> {

        private VkImageCreateInfo imageCreateInfo;
        private VmaAllocationCreateInfo allocationCreateInfo;
        private VkImageViewCreateInfo imageViewCreateInfo;

        public Builder() {
        }

        public Builder imageCreateInfo(VkImageCreateInfo imageCreateInfo) {
            this.imageCreateInfo = requireNonNull(imageCreateInfo);
            return this;
        }

        public Builder allocationCreateInfo(VmaAllocationCreateInfo allocationCreateInfo) {
            this.allocationCreateInfo = requireNonNull(allocationCreateInfo);
            return this;
        }

        public Builder imageViewCreateInfo(VkImageViewCreateInfo imageViewCreateInfo) {
            this.imageViewCreateInfo = requireNonNull(imageViewCreateInfo);
            return this;
        }

        @Override
        public VulkanRenderImage build() {

            VulkanImage image = new VulkanImage(requireNonNull(imageCreateInfo), requireNonNull(allocationCreateInfo));

            VulkanImageView imageView = new VulkanImageView(requireNonNull(imageViewCreateInfo.image(image.handle())));

            return new VulkanRenderImage(image, imageView);
        }
    }
}
