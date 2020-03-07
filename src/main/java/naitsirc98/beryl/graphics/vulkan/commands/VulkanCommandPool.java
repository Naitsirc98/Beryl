package naitsirc98.beryl.graphics.vulkan.commands;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.function.Consumer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.pointers;
import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;


public class VulkanCommandPool implements VulkanObject.Long {

    private final long vkCommandPool;
    private final int queueFamily;
    private final VkQueue queue;

    public VulkanCommandPool(VkQueue queue, int queueFamily) {
        this.queue = queue;
        this.queueFamily = queueFamily;
        vkCommandPool = createVkCommandPool();
    }

    @Override
    public long handle() {
        return vkCommandPool;
    }

    public VkQueue queue() {
        return queue;
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

            vkCall(vkAllocateCommandBuffers(logicalDevice().handle(), allocInfo, pCommandBuffers));

            VkCommandBuffer[] commandBuffers = new VkCommandBuffer[count];

            for(int i = 0;i < count;i++) {
                commandBuffers[i] = new VkCommandBuffer(pCommandBuffers.get(i), logicalDevice().handle());
            }

            return commandBuffers;
        }
    }

    public void freeCommandBuffer(VkCommandBuffer commandBuffer) {
        vkFreeCommandBuffers(logicalDevice().handle(), vkCommandPool, commandBuffer);
    }

    public void freeCommandBuffers(VkCommandBuffer... commandBuffers) {
        vkFreeCommandBuffers(logicalDevice().handle(), vkCommandPool, pointers(commandBuffers));
    }

    public void execute(Consumer<VkCommandBuffer> commandBufferConsumer) {

        try(MemoryStack stack = stackPush()) {

            VkCommandBuffer commandBuffer = newPrimaryCommandBuffer();

            beginCommandBufferTmp(commandBuffer, stack);

            commandBufferConsumer.accept(commandBuffer);

            endCommandBufferTmp(commandBuffer, stack);

            freeCommandBuffer(commandBuffer);
        }
    }

    private void endCommandBufferTmp(VkCommandBuffer commandBuffer, MemoryStack stack) {

        vkEndCommandBuffer(commandBuffer);

        VkSubmitInfo.Buffer submitInfo = VkSubmitInfo.callocStack(1, stack)
            .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
            .pCommandBuffers(stack.pointers(commandBuffer));

        vkQueueSubmit(queue, submitInfo, VK_NULL_HANDLE);

        vkQueueWaitIdle(queue);
    }

    private void beginCommandBufferTmp(VkCommandBuffer commandBuffer, MemoryStack stack) {
        vkBeginCommandBuffer(commandBuffer, VkCommandBufferBeginInfo.callocStack(stack)
                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                .flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT));
    }

    @Override
    public void free() {
        vkDestroyCommandPool(logicalDevice().handle(), vkCommandPool, null);
    }

    private long createVkCommandPool() {

        try(MemoryStack stack = stackPush()) {

            VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
                    .queueFamilyIndex(queueFamily)
                    .flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);

            LongBuffer pCommandPool = stack.mallocLong(1);

            vkCall(vkCreateCommandPool(logicalDevice().handle(), poolInfo, null, pCommandPool));

            return pCommandPool.get(0);
        }
    }
}
