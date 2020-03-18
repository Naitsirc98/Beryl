package naitsirc98.beryl.graphics.vulkan.descriptors;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.util.types.StackBuilder;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanDescriptorSetLayout implements VulkanObject.Long {

    private long vkDescriptorSetLayout;

    public VulkanDescriptorSetLayout(VkDescriptorSetLayoutBinding.Buffer bindings) {
        vkDescriptorSetLayout = createDescriptorSetLayout(bindings);
    }

    @Override
    public long handle() {
        return vkDescriptorSetLayout;
    }

    @Override
    public void free() {
        vkDestroyDescriptorSetLayout(Graphics.vulkan().logicalDevice().handle(), vkDescriptorSetLayout, null);
        vkDescriptorSetLayout = VK_NULL_HANDLE;
    }

    private long createDescriptorSetLayout(VkDescriptorSetLayoutBinding.Buffer bindings) {

        try(MemoryStack stack = stackPush()) {

            VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack)
                .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
                .pBindings(bindings);

            LongBuffer pDescriptorSetLayout = stack.mallocLong(1);

            VkDevice device = Graphics.vulkan().logicalDevice().handle();

            vkCall(vkCreateDescriptorSetLayout(device, layoutInfo, null, pDescriptorSetLayout));

            return pDescriptorSetLayout.get(0);
        }
    }

    public static final class Builder extends StackBuilder<VulkanDescriptorSetLayout> {

        private List<VkDescriptorSetLayoutBinding> bindings;

        public Builder() {
            bindings = new ArrayList<>(4);
        }

        public Builder binding(int binding, int type, int count, LongBuffer immutableSamplers, int stageFlags) {
            return binding(VkDescriptorSetLayoutBinding.callocStack()
                .binding(binding)
                .descriptorType(type)
                .descriptorCount(count)
                .pImmutableSamplers(immutableSamplers)
                .stageFlags(stageFlags));
        }

        public Builder binding(VkDescriptorSetLayoutBinding binding) {
            bindings.add(binding);
            return this;
        }

        @Override
        public VulkanDescriptorSetLayout build() {

            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.callocStack(this.bindings.size());

            for(int i = 0;i < bindings.capacity();i++) {
                bindings.get(i).set(this.bindings.get(i));
            }

            return new VulkanDescriptorSetLayout(bindings);
        }

        @Override
        public void pop() {
            super.pop();
            bindings = null;
        }
    }

}
