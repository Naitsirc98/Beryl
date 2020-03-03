package naitsirc98.beryl.graphics.vulkan.commands;

import naitsirc98.beryl.logging.Log;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static naitsirc98.beryl.graphics.vulkan.commands.VulkanCommandBuilder.COMMAND_BUFFERS_COUNT;
import static naitsirc98.beryl.graphics.vulkan.commands.VulkanCommandBuilder.buildCommandBuffers;

public class VulkanCommandBuilderExecutor {

    private final ExecutorService threadPool;

    public VulkanCommandBuilderExecutor() {
        this.threadPool = newFixedThreadPool(1);//newCachedThreadPool();
    }

    public void build(int count, VkCommandBuffer primaryCommandBuffer, VulkanCommandBufferConsumer consumer) {

        final int numBatches = (int)((float)count / COMMAND_BUFFERS_COUNT) + 1;

        buildCommandBuffers(0, count, primaryCommandBuffer, consumer);

        for(int i = 0;i < numBatches;i++) {
            final int offset = i * COMMAND_BUFFERS_COUNT;
            final int bufferCount = Math.min(COMMAND_BUFFERS_COUNT, count - offset);
            threadPool.submit(() -> buildCommandBuffers(offset, bufferCount, primaryCommandBuffer, consumer));
        }

        waitFor();
    }

    private void waitFor() {
        try {
            threadPool.awaitTermination(1, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.error("Timeout error while waiting for command buffers to build", e);
        }
    }
}
