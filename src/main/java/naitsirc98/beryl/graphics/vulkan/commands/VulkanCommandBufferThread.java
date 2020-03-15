package naitsirc98.beryl.graphics.vulkan.commands;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.graphics.vulkan.rendering.VulkanRenderer;
import naitsirc98.beryl.logging.Log;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_SECONDARY;

public final class VulkanCommandBufferThread<T extends VulkanThreadData> implements VulkanObject {

    private ExecutorService worker;
    private VulkanCommandPool commandPool;
    private VkCommandBuffer[] commandBuffers;
    private Throwable error;
    private T threadData;

    public VulkanCommandBufferThread(T threadData) {
        worker = newSingleThreadExecutor();
        commandPool = createCommandPool();
        commandBuffers = createCommandBuffers();
        this.threadData = requireNonNull(threadData);
    }

    public void submit(Runnable task) {
        worker.submit(task);
    }

    public void error(Throwable error) {
        this.error = error;
    }

    public Throwable error() {
        return error;
    }

    public VkCommandBuffer commandBuffer() {
        return commandBuffer(VulkanRenderer.get().currentSwapchainImageIndex());
    }

    public VkCommandBuffer commandBuffer(int index) {
        return commandBuffers[index];
    }

    public T threadData() {
        return threadData;
    }

    @Override
    public void free() {

        stopWorker();
        threadData.free();
        commandPool.freeCommandBuffers(commandBuffers);
        commandPool.free();

        worker = null;
        threadData = null;
        commandBuffers = null;
        commandPool = null;
    }

    private VkCommandBuffer[] createCommandBuffers() {
        return commandPool.newCommandBuffers(VK_COMMAND_BUFFER_LEVEL_SECONDARY, swapchain().imageCount());
    }

    private VulkanCommandPool createCommandPool() {
        return new VulkanCommandPool(
                Graphics.vulkan().logicalDevice().graphicsQueue(),
                Graphics.vulkan().physicalDevice().queueFamilyIndices().graphicsFamily()
        );
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
