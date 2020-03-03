package naitsirc98.beryl.graphics.vulkan.commands;

import naitsirc98.beryl.graphics.Graphics;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import static java.lang.ThreadLocal.withInitial;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_SECONDARY;
import static org.lwjgl.vulkan.VK10.vkCmdExecuteCommands;

public class VulkanCommandBuilder implements NativeResource {

    public static final int COMMAND_BUFFERS_COUNT = 128;
    public static final int MIN_PUSH_CONSTANT_DATA_SIZE = 128;

    private static final ThreadLocal<VulkanCommandBuilder> COMMAND_GENERATOR = withInitial(VulkanCommandBuilder::new);

    public static void buildCommandBuffers(int offset, VkCommandBuffer primaryCommandBuffer, VulkanCommandBufferConsumer consumer) {

        VulkanCommandBuilder generator = COMMAND_GENERATOR.get();

        VkCommandBuffer[] commandBuffers = generator.commandBuffers;
        ByteBuffer pushConstantData = generator.pushConstantData;

        for(int i = offset;i < COMMAND_BUFFERS_COUNT + offset;i++) {
            consumer.consume(i, commandBuffers[i], pushConstantData);
        }

        vkCmdExecuteCommands(primaryCommandBuffer, generator.pCommandBuffers);
    }

    private VulkanCommandPool commandPool;
    private VkCommandBuffer[] commandBuffers;
    private PointerBuffer pCommandBuffers;
    private ByteBuffer pushConstantData;

    private VulkanCommandBuilder() {
        commandPool = createCommandPool();
        commandBuffers = commandPool.newCommandBuffers(VK_COMMAND_BUFFER_LEVEL_SECONDARY, COMMAND_BUFFERS_COUNT);
        pCommandBuffers = getCommandBufferPointers();
        pushConstantData = memAlloc(MIN_PUSH_CONSTANT_DATA_SIZE);
    }

    private PointerBuffer getCommandBufferPointers() {

        LongBuffer pointers = memAllocLong(COMMAND_BUFFERS_COUNT);

        for(int i = 0;i < COMMAND_BUFFERS_COUNT;i++) {
            pCommandBuffers.put(i, commandBuffers[i]);
        }

        return pCommandBuffers;
    }

    private VulkanCommandPool createCommandPool() {
        return new VulkanCommandPool(
                Graphics.vulkan().logicalDevice().graphicsQueue(),
                Graphics.vulkan().physicalDevice().queueFamilyIndices().graphicsFamily()
        );
    }

    @Override
    public void free() {

        memFree(pushConstantData);

        memFree(pCommandBuffers);
        pCommandBuffers = null;

        commandPool.freeCommandBuffers(commandBuffers);
        commandBuffers = null;

        commandPool.free();
        commandPool = null;
    }
}
