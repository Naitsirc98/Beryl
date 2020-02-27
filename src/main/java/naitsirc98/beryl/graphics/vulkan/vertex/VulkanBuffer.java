package naitsirc98.beryl.graphics.vulkan.vertex;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.vulkan.util.VulkanMemoryUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;

import java.nio.LongBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanBuffer implements NativeResource {

    protected static VkDevice device() {
        return Graphics.vulkan().vkLogicalDevice();
    }

    protected long vkBuffer;
    protected long vkMemory;
    protected final int size;
    protected final int type;

    protected VulkanBuffer(int size, int type, int desiredMemoryProperties) {
        this.size = size;
        this.type = type;
        vkBuffer = createVkBuffer();
        this.vkMemory = VulkanMemoryUtils.allocateMemoryFor(vkBuffer, desiredMemoryProperties);
        vkBindBufferMemory(device(), vkBuffer, vkMemory, 0);
    }

    public final long vkBuffer() {
        return vkBuffer;
    }

    public final long vkMemory() {
        return vkMemory;
    }

    @Override
    public void free() {
        vkDestroyBuffer(device(), vkBuffer, null);
        vkFreeMemory(device(), vkMemory, null);
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
            vkCall(vkCreateBuffer(device(), bufferInfo, null, pBuffer));

            return pBuffer.get(0);
        }
    }

}
