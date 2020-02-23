package naitsirc98.beryl.graphics.vulkan.commands;

import naitsirc98.beryl.graphics.vulkan.devices.VulkanDevice;
import naitsirc98.beryl.util.Destructor;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;

import java.nio.LongBuffer;

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
