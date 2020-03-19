package naitsirc98.beryl.graphics.vulkan.descriptors;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;

import java.nio.LongBuffer;

import static java.lang.Math.min;
import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static naitsirc98.beryl.util.Asserts.assertThat;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanDescriptorPool implements VulkanObject.Long {

    private long handle;
    private VkDescriptorPoolCreateInfo poolInfo;
    private LongBuffer descriptorSets;
    private final VulkanDescriptorSetLayout descriptorSetLayout;

    public VulkanDescriptorPool(VulkanDescriptorSetLayout descriptorSetLayout, int capacity, int initialSize, int descriptorType) {
        this.descriptorSetLayout = descriptorSetLayout;
        init(capacity, initialSize, descriptorType);
    }

    public VulkanDescriptorPool(VulkanDescriptorSetLayout descriptorSetLayout, int capacity, int initialSize, int... descriptorTypes) {
        this.descriptorSetLayout = descriptorSetLayout;
        init(capacity, initialSize, assertThat(descriptorTypes, descriptorTypes.length > 0));
    }

    @Override
    public long handle() {
        return handle;
    }

    public int capacity() {
        return descriptorSets.capacity();
    }

    public int size() {
        return descriptorSets.limit();
    }

    public long descriptorSet(int index) {
        return descriptorSets.get(index);
    }

    public void ensure(int descriptorSetsCount) {

        if (size() >= descriptorSetsCount) {
            return;
        }

        if (descriptorSetsCount > capacity()) {
            reallocatePool(size() + descriptorSetsCount);
        }

        if(descriptorSetsCount - size() <= 0) {
            return;
        }

        allocateDescriptorSets(descriptorSetsCount - size());
    }

    public void reallocatePool(int newCapacity) {

        final int size = min(newCapacity, size());

        poolInfo.maxSets(newCapacity);

        vkDestroyDescriptorPool(logicalDevice().handle(), handle, null);

        createVkDescriptorPool();

        allocateDescriptorSets(size);
    }

    public void allocateDescriptorSets(int descriptorSetsCount) {
        assertTrue(size() + descriptorSetsCount <= capacity());

        try (MemoryStack stack = stackPush()) {

            LongBuffer layouts = memAllocLong(descriptorSetsCount);

            for (int i = 0; i < layouts.capacity(); i++) {
                layouts.put(i, descriptorSetLayout.handle());
            }

            VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
                    .descriptorPool(handle)
                    .pSetLayouts(layouts);

            descriptorSets.position(size()).limit(size() + descriptorSetsCount);

            vkCall(vkAllocateDescriptorSets(logicalDevice().handle(), allocInfo, descriptorSets));

            descriptorSets.position(0);

            memFree(layouts);
        }
    }

    public void reset() {
        vkResetDescriptorPool(logicalDevice().handle(), handle, 0);
        descriptorSets.limit(0);
    }

    @Override
    public void free() {

        vkDestroyDescriptorPool(logicalDevice().handle(), handle, null);
        poolInfo.free();
        memFree(descriptorSets);

        handle = VK_NULL_HANDLE;
        poolInfo = null;
        descriptorSets = null;
    }

    private void init(int capacity, int initialSize, int... descriptorTypes) {
        assertTrue(initialSize <= capacity);

        VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.create(descriptorTypes.length);

        for (int i = 0; i < descriptorTypes.length; i++) {

            VkDescriptorPoolSize poolSize = poolSizes.get(i);
            poolSize.type(descriptorTypes[i]);
            poolSize.descriptorCount(1);
        }

        poolInfo = VkDescriptorPoolCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
                .pPoolSizes(poolSizes)
                //  .flags(VK_DESCRIPTOR_POOL_CREATE_UPDATE_AFTER_BIND_BIT_EXT)
                .maxSets(capacity);

        createVkDescriptorPool();

        if(initialSize > 0) {
            allocateDescriptorSets(initialSize);
        }
    }

    private void createVkDescriptorPool() {

        try (MemoryStack stack = stackPush()) {

            LongBuffer pDescriptorPool = stack.mallocLong(1);

            vkCall(vkCreateDescriptorPool(logicalDevice().handle(), poolInfo, null, pDescriptorPool));

            handle = pDescriptorPool.get(0);

            if (descriptorSets != null) {
                descriptorSets = memRealloc(descriptorSets, poolInfo.maxSets());
            } else {
                descriptorSets = memAllocLong(poolInfo.maxSets());
            }

            descriptorSets.limit(0);
        }
    }
}
