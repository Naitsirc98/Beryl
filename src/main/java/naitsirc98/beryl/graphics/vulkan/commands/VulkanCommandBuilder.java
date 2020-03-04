package naitsirc98.beryl.graphics.vulkan.commands;

import naitsirc98.beryl.concurrency.Worker;
import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.vulkan.rendering.VulkanRenderer;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_SECONDARY;

public class VulkanCommandBuilder implements NativeResource {

    public static final int MIN_PUSH_CONSTANT_DATA_SIZE = 128;


    private final Worker worker;
    private final VulkanRenderer renderer;
    private VulkanCommandPool commandPool;
    private VkCommandBuffer[] commandBuffers;
    private ByteBuffer pushConstantData;

    public VulkanCommandBuilder() {
        worker = new Worker("VulkanCommandBufferBuilder-Worker"+hashCode()).start();
        renderer = Graphics.vulkan().renderer();
        commandPool = createCommandPool();
        commandBuffers = createCommandBuffers();
        pushConstantData = memAlloc(MIN_PUSH_CONSTANT_DATA_SIZE);
    }

    public void await() {
        worker.await();
    }

    public void submitRecordTask(Runnable task) {
        worker.submit(task);
    }

    public VkCommandBuffer commandBuffer() {
        return commandBuffer(renderer.currentSwapchainImageIndex());
    }

    public VkCommandBuffer commandBuffer(int index) {
        return commandBuffers[index];
    }

    public ByteBuffer pushConstantData() {
        return pushConstantData;
    }

    private VkCommandBuffer[] createCommandBuffers() {

        final int count = Graphics.vulkan().swapchain().swapChainImages().length;

        return commandPool.newCommandBuffers(VK_COMMAND_BUFFER_LEVEL_SECONDARY, count);
    }

    private VulkanCommandPool createCommandPool() {
        return new VulkanCommandPool(
                Graphics.vulkan().logicalDevice().graphicsQueue(),
                Graphics.vulkan().physicalDevice().queueFamilyIndices().graphicsFamily()
        );
    }

    @Override
    public void free() {

        worker.terminate();

        memFree(pushConstantData);
        pushConstantData = null;

        commandPool.freeCommandBuffers(commandBuffers);
        commandBuffers = null;

        commandPool.free();
        commandPool = null;
    }
}
