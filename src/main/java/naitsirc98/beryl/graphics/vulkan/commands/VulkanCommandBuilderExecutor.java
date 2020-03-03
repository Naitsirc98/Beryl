package naitsirc98.beryl.graphics.vulkan.commands;

import naitsirc98.beryl.logging.Log;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static naitsirc98.beryl.graphics.vulkan.commands.VulkanCommandBuilder.COMMAND_BUFFERS_COUNT;
import static naitsirc98.beryl.graphics.vulkan.commands.VulkanCommandBuilder.buildCommandBuffers;

public class VulkanCommandBuilderExecutor {

    private final ExecutorService threadPool;

    public VulkanCommandBuilderExecutor() {
        this.threadPool = newFixedThreadPool(3);//newCachedThreadPool();
    }

    public void build(int count, VkCommandBuffer primaryCommandBuffer, VulkanCommandBufferConsumer consumer) {

        final int numBatches = (int)((float)count / COMMAND_BUFFERS_COUNT) + 1;
        CountDownLatch latch = new CountDownLatch(numBatches);

        try {

            for(int i = 0;i < numBatches;i++) {
                final int offset = i * COMMAND_BUFFERS_COUNT;
                final int bufferCount = Math.min(COMMAND_BUFFERS_COUNT, count - offset);
                threadPool.submit(() -> {
                    buildCommandBuffers(offset, bufferCount, primaryCommandBuffer, consumer);
                    latch.countDown();
                });
            }

        } finally {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Log.error("Timeout error while waiting for command buffer recording", e);
            }
        }
    }
}
