package naitsirc98.beryl.graphics.vulkan.descriptors;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;

import java.nio.LongBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanDescriptorSets implements VulkanObject {

    private LongBuffer descriptorSets;

    public VulkanDescriptorSets(VulkanDescriptorPool descriptorPool, VulkanDescriptorSetLayout descriptorSetLayout, int count) {
        init(descriptorPool, descriptorSetLayout, count);
    }

    public LongBuffer descriptorSets() {
        return descriptorSets;
    }

    @Override
    public void free() {
        memFree(descriptorSets);
        descriptorSets = null;
    }

    private void init(VulkanDescriptorPool descriptorPool, VulkanDescriptorSetLayout descriptorSetLayout, int count) {

        try(MemoryStack stack = stackPush()) {

            LongBuffer layouts = stack.mallocLong(count);

            for(int i = 0;i < layouts.capacity();i++) {
                layouts.put(i, descriptorSetLayout.handle());
            }

            VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.callocStack(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
            allocInfo.descriptorPool(descriptorPool.handle());
            allocInfo.pSetLayouts(layouts);

            descriptorSets = memAllocLong(count);

            vkCall(vkAllocateDescriptorSets(logicalDevice().handle(), allocInfo, descriptorSets));
        }
    }
}
