package naitsirc98.beryl.graphics.vulkan.commands;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.vulkan.rendering.VulkanRenderer;
import naitsirc98.beryl.logging.Log;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_SECONDARY;

public class VulkanCommandBuilder implements NativeResource {

    public static final int MIN_PUSH_CONSTANT_DATA_SIZE = 128;


    private final ExecutorService worker;
    private VulkanCommandPool commandPool;
    private VkCommandBuffer[] commandBuffers;
    private ByteBuffer pushConstantData;

    public VulkanCommandBuilder() {
        worker = newSingleThreadExecutor();
        commandPool = createCommandPool();
        commandBuffers = createCommandBuffers();
        pushConstantData = memAlloc(MIN_PUSH_CONSTANT_DATA_SIZE);
    }

    public void submitRecordTask(Runnable task) {
        worker.submit(task);
    }

    public VkCommandBuffer commandBuffer() {
        return commandBuffer(VulkanRenderer.get().currentSwapchainImageIndex());
    }

    public VkCommandBuffer commandBuffer(int index) {
        return commandBuffers[index];
    }

    public ByteBuffer pushConstantData() {
        return pushConstantData;
    }

    private VkCommandBuffer[] createCommandBuffers() {

        final int count = Graphics.vulkan().swapchain().imageCount();

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

        stopWorker();

        memFree(pushConstantData);
        pushConstantData = null;

        commandPool.freeCommandBuffers(commandBuffers);
        commandBuffers = null;

        commandPool.free();
        commandPool = null;
    }

    private void stopWorker() {
        worker.shutdown();
        try {
            worker.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.error("Timeout error while waiting for VulkanCommandBuilder to finish");
        }
    }
}
