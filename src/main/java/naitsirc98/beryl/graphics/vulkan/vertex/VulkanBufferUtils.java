package naitsirc98.beryl.graphics.vulkan.vertex;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.vulkan.commands.VulkanCommandPool;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanMemoryUtils.allocateVkBufferMemory;
import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public final class VulkanBufferUtils {

    public static long newVkBuffer(int size, int type) {

        try(MemoryStack stack = stackPush()) {

            VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                    .size(size)
                    .usage(type)
                    .sharingMode(VK_SHARING_MODE_EXCLUSIVE);

            LongBuffer pBuffer = stack.mallocLong(1);
            vkCall(vkCreateBuffer(Graphics.vulkan().logicalDevice().handle(), bufferInfo, null, pBuffer));

            return pBuffer.get(0);
        }
    }

    public static void setVulkanBufferData(long buffer, ByteBuffer data) {

        try(MemoryStack stack = stackPush()) {

            LongBuffer stagingBuffer = createStagingBuffer(stack, data);

            copyVkBuffer(stagingBuffer.get(0), buffer, data.limit());

            vkDestroyBuffer(Graphics.vulkan().logicalDevice().handle(), stagingBuffer.get(0), null);
            vkFreeMemory(Graphics.vulkan().logicalDevice().handle(), stagingBuffer.get(1), null);
        }
    }

    public static void copyVkBuffer(long srcBuffer, long dstBuffer, long size) {

        try(MemoryStack stack = stackPush()) {

            VulkanCommandPool commandPool = Graphics.vulkan().graphicsCommandPool();
            VkQueue graphicsQueue = Graphics.vulkan().logicalDevice().graphicsQueue();

            VkCommandBuffer commandBuffer = commandPool.newPrimaryCommandBuffer();

            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            beginInfo.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

            vkCall(vkBeginCommandBuffer(commandBuffer, beginInfo));

            VkBufferCopy.Buffer copyRegion = VkBufferCopy.callocStack(1, stack);
            copyRegion.size(size);

            vkCmdCopyBuffer(commandBuffer, srcBuffer, dstBuffer, copyRegion);

            vkCall(vkEndCommandBuffer(commandBuffer));

            VkSubmitInfo.Buffer submitInfo = VkSubmitInfo.callocStack(1, stack);
            submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.pCommandBuffers(stack.pointers(commandBuffer));

            vkCall(vkQueueSubmit(graphicsQueue, submitInfo, VK_NULL_HANDLE));

            vkCall(vkQueueWaitIdle(graphicsQueue));

            Graphics.vulkan().graphicsCommandPool().freeCommandBuffers(commandBuffer);
        }
    }

    private static LongBuffer createStagingBuffer(MemoryStack stack, ByteBuffer data) {

        VkDevice device = Graphics.vulkan().logicalDevice().handle();

        LongBuffer stagingBuffer = stack.mallocLong(2);

        stagingBuffer.put(0, newVkBuffer(data.limit(), VK_BUFFER_USAGE_TRANSFER_SRC_BIT));
        stagingBuffer.put(1, allocateVkBufferMemory(stagingBuffer.get(0),
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT));

        vkBindBufferMemory(device, stagingBuffer.get(0), stagingBuffer.get(1), 0);

        PointerBuffer stagingMemoryPtr = stack.mallocPointer(1);

        vkCall(vkMapMemory(device, stagingBuffer.get(1), 0, data.limit(), 0, stagingMemoryPtr));

        stagingMemoryPtr.getByteBuffer(0, data.limit()).put(data);

        vkUnmapMemory(device, stagingBuffer.get(1));

        data.rewind();

        return stagingBuffer;
    }

    private VulkanBufferUtils() {}
}
