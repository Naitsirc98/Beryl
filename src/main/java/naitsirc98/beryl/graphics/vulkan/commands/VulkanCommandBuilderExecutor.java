package naitsirc98.beryl.graphics.vulkan.commands;

import naitsirc98.beryl.logging.Log;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newCachedThreadPool;
import static naitsirc98.beryl.graphics.vulkan.commands.VulkanCommandBuilder.COMMAND_BUFFERS_COUNT;
import static naitsirc98.beryl.graphics.vulkan.commands.VulkanCommandBuilder.buildCommandBuffers;

public class VulkanCommandBuilderExecutor {

    private final ExecutorService threadPool;

    public VulkanCommandBuilderExecutor() {
        this.threadPool = newCachedThreadPool();
    }

    public void build(int count, VkCommandBuffer primaryCommandBuffer, VulkanCommandBufferConsumer consumer) {

        final int numBatches = count / COMMAND_BUFFERS_COUNT;

        for(int i = 0;i < numBatches;i++) {
            final int offset = i * COMMAND_BUFFERS_COUNT;
            threadPool.submit(() -> buildCommandBuffers(COMMAND_BUFFERS_COUNT, primaryCommandBuffer, consumer));
        }

        waitFor();
    }

    private void waitFor() {
        try {
            threadPool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.error("Timeout error while waiting for command buffers to build", e);
        }
    }
}
