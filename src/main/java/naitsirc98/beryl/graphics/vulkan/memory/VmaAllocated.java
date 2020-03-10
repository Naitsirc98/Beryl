package naitsirc98.beryl.graphics.vulkan.memory;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocationInfo;

import static org.lwjgl.util.vma.Vma.vmaGetAllocationInfo;
import static org.lwjgl.util.vma.Vma.vmaTouchAllocation;

public interface VmaAllocated extends VulkanObject {

    long allocation();

    VmaAllocationCreateInfo allocationCreateInfo();

    default VmaAllocationInfo allocationInfo() {
        VmaAllocationInfo allocationInfo = VmaAllocationInfo.create();
        vmaGetAllocationInfo(allocator().handle(), allocation(), allocationInfo);
        return allocationInfo;
    }

    default long offset() {
        return allocationInfo().offset();
    }

    default boolean available() {
        return vmaTouchAllocation(allocator().handle(), allocation());
    }

    void ensure(); // VMA lost allocations

}
