package naitsirc98.beryl.graphics.vulkan.descriptors;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;

import java.nio.LongBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static naitsirc98.beryl.util.Asserts.assertThat;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanDescriptorPool implements VulkanObject.Long {

    private long handle;

    public VulkanDescriptorPool(int maxSets, int descriptorType) {
        init(maxSets, descriptorType);
    }

    public VulkanDescriptorPool(int maxSets, int... descriptorTypes) {
        init(maxSets, assertThat(descriptorTypes, descriptorTypes.length > 0));
    }

    @Override
    public long handle() {
        return handle;
    }

    @Override
    public void free() {
        vkDestroyDescriptorPool(logicalDevice().handle(), handle, null);
        handle = VK_NULL_HANDLE;
    }

    private void init(int maxSets, int... descriptorTypes) {

        try(MemoryStack stack = stackPush()) {

            VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.callocStack(descriptorTypes.length, stack);

            for(int i = 0;i < descriptorTypes.length;i++) {

                VkDescriptorPoolSize poolSize = poolSizes.get(i);
                poolSize.type(descriptorTypes[i]);
                poolSize.descriptorCount(1);
            }

            VkDescriptorPoolCreateInfo poolInfo = VkDescriptorPoolCreateInfo.callocStack(stack);
            poolInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
            poolInfo.pPoolSizes(poolSizes);
            poolInfo.maxSets(maxSets);

            LongBuffer pDescriptorPool = stack.mallocLong(1);

            vkCall(vkCreateDescriptorPool(logicalDevice().handle(), poolInfo, null, pDescriptorPool));

            handle = pDescriptorPool.get(0);
        }
    }
}
