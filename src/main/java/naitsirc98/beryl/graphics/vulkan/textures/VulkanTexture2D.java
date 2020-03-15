package naitsirc98.beryl.graphics.vulkan.textures;

import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.Image;
import naitsirc98.beryl.images.PixelFormat;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkExtent3D;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_GPU_ONLY;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanTexture2D extends VulkanTexture implements Texture2D {

    public VulkanTexture2D() {
    }

    private VkImageViewCreateInfo getTexture2DImageViewCreateInfo() {

        VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                .viewType(VK_IMAGE_VIEW_TYPE_2D);
                // .format(format); Set later

        viewInfo.subresourceRange()
                .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                .baseMipLevel(0)
                // .levelCount(1) Set later
                .baseArrayLayer(0)
                .layerCount(1);

        return viewInfo;
    }

    public void validate() {
        // Must be executed in the same thread across multiple textures
        // if(renderImage.image().layout() != VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {
        //     renderImage.image().transitionLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        // }
        sampler.validate();
    }

    @Override
    public Type type() {
        return Type.TEXTURE_2D;
    }

    @Override
    public int width() {
        return renderImage.width();
    }

    @Override
    public int height() {
        return renderImage.height();
    }

    @Override
    public void allocate(int mipLevels, int width, int height, PixelFormat internalFormat) {

        try(MemoryStack stack = stackPush()) {

            VkImageCreateInfo imageCreateInfo = VkImageCreateInfo.calloc()
                    .sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
                    .imageType(VK_IMAGE_TYPE_2D)
                    .arrayLayers(1)
                    .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                    .format(mapper().mapToAPI(internalFormat))
                    .mipLevels(mipLevels)
                    .extent(VkExtent3D.callocStack(stack).set(width, height, 1))
                    .samples(VK_SAMPLE_COUNT_1_BIT)
                    .sharingMode(VK_SHARING_MODE_EXCLUSIVE)
                    .usage(VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT)
                    .tiling(VK_IMAGE_TILING_OPTIMAL);

            VmaAllocationCreateInfo allocationCreateInfo = VmaAllocationCreateInfo.calloc()
                    .usage(VMA_MEMORY_USAGE_GPU_ONLY);

            renderImage.image().init(allocator().createImage(imageCreateInfo, allocationCreateInfo));

            VkImageViewCreateInfo viewInfo = getTexture2DImageViewCreateInfo()
                    .image(renderImage.image().handle())
                    .format(imageCreateInfo.format());

            viewInfo.subresourceRange().levelCount(mipLevels);

            renderImage.view().init(viewInfo);
        }
    }

    @Override
    public void pixels(int mipLevels, Image image) {
        allocate(mipLevels, image.width(), image.height(), image.pixelFormat());
        if(image.pixelFormat().dataType().decimal()) {
            update(0, 0, 0, image.width(), image.height(), image.pixelFormat(), image.pixelsf());
        } else {
            update(0, 0, 0, image.width(), image.height(), image.pixelFormat(), image.pixelsi());
        }
    }

    @Override
    public void pixels(int mipLevels, int width, int height, PixelFormat format, ByteBuffer pixels) {
        allocate(mipLevels, width, height, format);
        renderImage.image().pixels(0, 0, 0, 0, pixels);
        renderImage.image().transitionLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
    }

    @Override
    public void update(int mipLevel, int xOffset, int yOffset, int width, int height, PixelFormat format, ByteBuffer pixels) {
        // TODO
        renderImage.image().pixels(mipLevel, xOffset, yOffset, 0, pixels);
        renderImage.image().transitionLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
    }

    @Override
    public void update(int mipLevel, int xOffset, int yOffset, int width, int height, PixelFormat format, FloatBuffer pixels) {
        // TODO
        renderImage.image().pixels(mipLevel, xOffset, yOffset, 0, pixels);
        renderImage.image().transitionLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
    }
}
