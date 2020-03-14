package naitsirc98.beryl.graphics.vulkan.descriptors;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;

public class VulkanDescriptorSet implements VulkanObject.Long {

    private long handle;

    @Override
    public long handle() {
        return handle;
    }

    @Override
    public void free() {

    }
}
