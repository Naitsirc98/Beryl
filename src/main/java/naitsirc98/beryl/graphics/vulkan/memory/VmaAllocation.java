package naitsirc98.beryl.graphics.vulkan.memory;

import org.lwjgl.util.vma.VmaAllocationCreateInfo;

public abstract class VmaAllocation {

    private final long vmaAllocation;
    private final VmaAllocationCreateInfo allocationCreateInfo;
    private final long resource;

    public VmaAllocation(long vmaAllocation, VmaAllocationCreateInfo allocationCreateInfo, long resource) {
        this.vmaAllocation = vmaAllocation;
        this.allocationCreateInfo = allocationCreateInfo;
        this.resource = resource;
    }

    public long resource() {
        return resource;
    }

    public long allocation() {
        return vmaAllocation;
    }

    public VmaAllocationCreateInfo allocationCreateInfo() {
        return allocationCreateInfo;
    }
}
