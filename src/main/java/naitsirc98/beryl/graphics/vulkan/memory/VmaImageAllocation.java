package naitsirc98.beryl.graphics.vulkan.memory;

import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkImageCreateInfo;

public final class VmaImageAllocation extends VmaAllocation {

    private final VkImageCreateInfo imageCreateInfo;

    public VmaImageAllocation(long vmaAllocation, VmaAllocationCreateInfo allocationCreateInfo,
                              long vkImage, VkImageCreateInfo imageCreateInfo) {
        super(vmaAllocation, allocationCreateInfo, vkImage);
        this.imageCreateInfo = imageCreateInfo;
    }

    public long image() {
        return resource();
    }

    public VkImageCreateInfo imageCreateInfo() {
        return imageCreateInfo;
    }

}
