package naitsirc98.beryl.graphics.vulkan.commands;

import naitsirc98.beryl.graphics.vulkan.devices.VulkanDevice;
import naitsirc98.beryl.util.Destructor;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.pointers;
import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

@Destructor
public class VulkanCommandPool implements NativeResource {

    private final long vkCommandPool;
    private final int queueFamily;
    private final VulkanDevice device;

    public VulkanCommandPool(VulkanDevice device, int queueFamily) {
        this.device = device;
        this.queueFamily = queueFamily;
        vkCommandPool = createVkCommandPool();
    }

    public long vkCommandPool() {
        return vkCommandPool;
    }

    public VkCommandBuffer newPrimaryCommandBuffer() {
        return newCommandBuffers(VK_COMMAND_BUFFER_LEVEL_PRIMARY, 1)[0];
    }

    public VkCommandBuffer[] newPrimaryCommandBuffers(int count) {
        return newCommandBuffers(VK_COMMAND_BUFFER_LEVEL_PRIMARY, count);
    }

    public VkCommandBuffer newSecondaryCommandBuffer() {
        return newCommandBuffers(VK_COMMAND_BUFFER_LEVEL_SECONDARY, 1)[0];
    }

    public VkCommandBuffer[] newSecondaryCommandBuffers(int count) {
        return newCommandBuffers(VK_COMMAND_BUFFER_LEVEL_SECONDARY, count);
    }

    public VkCommandBuffer[] newCommandBuffers(int level, int count) {

        try(MemoryStack stack = stackPush()) {

            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.callocStack(stack)
            .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
            .level(level)
            .commandPool(vkCommandPool)
            .commandBufferCount(count);

            PointerBuffer pCommandBuffers = stack.mallocPointer(count);

            VkDevice logicalDevice =  device.logicalDevice().vkDevice();

            vkCall(vkAllocateCommandBuffers(logicalDevice, allocInfo, pCommandBuffers));

            VkCommandBuffer[] commandBuffers = new VkCommandBuffer[count];

            for(int i = 0;i < count;i++) {
                commandBuffers[i] = new VkCommandBuffer(pCommandBuffers.get(i), logicalDevice);
            }

            return commandBuffers;
        }
    }

    public void destroy(VkCommandBuffer commandBuffer) {
        vkFreeCommandBuffers(device.logicalDevice().vkDevice(), vkCommandPool, commandBuffer);
    }

    public void destroy(VkCommandBuffer... commandBuffers) {
        vkFreeCommandBuffers(device.logicalDevice().vkDevice(), vkCommandPool, pointers(commandBuffers));
    }

    @Override
    public void free() {
        vkDestroyCommandPool(device.logicalDevice().vkDevice(), vkCommandPool, null);
    }

    private long createVkCommandPool() {

        try(MemoryStack stack = stackPush()) {

            VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.callocStack(stack)
                .sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
                .queueFamilyIndex(queueFamily);

            LongBuffer pCommandPool = stack.mallocLong(1);

            vkCall(vkCreateCommandPool(device.logicalDevice().vkDevice(), poolInfo, null, pCommandPool));

            return pCommandPool.get(0);
        }
    }
}
