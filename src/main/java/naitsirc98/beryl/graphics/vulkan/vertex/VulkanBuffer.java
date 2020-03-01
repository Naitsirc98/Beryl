package naitsirc98.beryl.graphics.vulkan.vertex;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.graphics.vulkan.util.VulkanMemoryUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferCreateInfo;

import java.nio.LongBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanBuffer implements VulkanObject.Long {

    protected long vkBuffer;
    protected long vkMemory;
    protected final int size;
    protected final int type;

    protected VulkanBuffer(int size, int type, int desiredMemoryProperties) {
        this.size = size;
        this.type = type;
        vkBuffer = createVkBuffer();
        this.vkMemory = VulkanMemoryUtils.allocateVkBufferMemory(vkBuffer, desiredMemoryProperties);
        vkBindBufferMemory(logicalDevice().handle(), vkBuffer, vkMemory, 0);
    }

    @Override
    public final long handle() {
        return vkBuffer;
    }

    public final long vkMemory() {
        return vkMemory;
    }

    @Override
    public void free() {
        vkDestroyBuffer(logicalDevice().handle(), vkBuffer, null);
        vkFreeMemory(logicalDevice().handle(), vkMemory, null);
        vkBuffer = VK_NULL_HANDLE;
        vkMemory = VK_NULL_HANDLE;
    }

    protected long createVkBuffer() {

        try(MemoryStack stack = stackPush()) {

            VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                    .size(size)
                    .usage(type)
                    .sharingMode(VK_SHARING_MODE_EXCLUSIVE);

            LongBuffer pBuffer = stack.mallocLong(1);
            vkCall(vkCreateBuffer(logicalDevice().handle(), bufferInfo, null, pBuffer));

            return pBuffer.get(0);
        }
    }

}
