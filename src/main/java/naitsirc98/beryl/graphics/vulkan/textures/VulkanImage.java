package naitsirc98.beryl.graphics.vulkan.textures;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.graphics.vulkan.memory.VmaAllocated;
import naitsirc98.beryl.graphics.vulkan.memory.VmaImageAllocation;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkImageCreateInfo;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static naitsirc98.beryl.graphics.vulkan.buffers.VulkanBufferUtils.transferToImage;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;

public class VulkanImage implements VmaAllocated, VulkanObject.Long {

    private long vkImage;
    private long allocation;
    private VkImageCreateInfo imageInfo;
    private VmaAllocationCreateInfo allocationCreateInfo;

    public VulkanImage() {
    }

    public VulkanImage(VkImageCreateInfo imageInfo, VmaAllocationCreateInfo allocationInfo) {
        init(allocator().createImage(imageInfo, allocationInfo));
    }

    public void init(VmaImageAllocation imageAllocation) {
        this.vkImage = imageAllocation.image();
        this.allocation = imageAllocation.allocation();
        this.imageInfo = imageAllocation.imageCreateInfo();
        this.allocationCreateInfo = imageAllocation.allocationCreateInfo();
    }

    @Override
    public long handle() {
        return vkImage;
    }

    @Override
    public long allocation() {
        return allocation;
    }

    public void resize(int width, int height, int depth) {

        if(width() == width && height() == height && depth() == depth) {
            return;
        }

        allocator().destroyImage(vkImage, allocation);

        imageInfo.extent().set(width, height, depth);

        init(allocator().createImage(imageInfo, allocationCreateInfo));
    }

    public void pixels(ByteBuffer pixels) {
        transferToImage(0, pixels, this);
    }

    public void pixels(FloatBuffer pixels) {
        transferToImage(0, pixels, this);
    }

    public int format() {
        return imageInfo.format();
    }

    public int tiling() {
        return imageInfo.tiling();
    }

    public int width() {
        return imageInfo.extent().width();
    }

    public int height() {
        return imageInfo.extent().height();
    }

    public int depth() {
        return imageInfo.extent().depth();
    }

    public int mipLevels() {
        return imageInfo.mipLevels();
    }

    public int arrayLayers() {
        return imageInfo.arrayLayers();
    }

    public int type() {
        return imageInfo.imageType();
    }

    public int initialLayout() {
        return imageInfo.initialLayout();
    }

    public int samples() {
        return imageInfo.samples();
    }

    public int sharingMode() {
        return imageInfo.sharingMode();
    }

    public int flags() {
        return imageInfo.flags();
    }

    @Override
    public VmaAllocationCreateInfo allocationCreateInfo() {
        return allocationCreateInfo;
    }

    @Override
    public void ensure() {
        // TODO
    }

    @Override
    public void free() {

        if(vkImage == VK_NULL_HANDLE) {
            return;
        }

        allocator().destroyImage(vkImage, allocation);
        imageInfo.free();
        allocationCreateInfo.free();


        vkImage = VK_NULL_HANDLE;
        allocation = VK_NULL_HANDLE;
        imageInfo = null;
        allocationCreateInfo = null;
    }
}
