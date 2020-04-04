package naitsirc98.beryl.graphics.vulkan.commands;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.vulkan.rendering.VulkanRenderer;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.resources.Resource;
import naitsirc98.beryl.util.SystemInfo;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.range;
import static naitsirc98.beryl.util.Asserts.assertThat;
import static org.joml.Math.min;
import static org.joml.Math.round;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.vulkan.VK10.vkCmdExecuteCommands;

public class VulkanCommandBufferThreadExecutor<T extends VulkanThreadData> implements Resource {

    private VulkanCommandBufferThread<T>[] commandBufferThreads;
    private PointerBuffer[] pCommandBuffers;
    private final int threadCount;
    private volatile boolean errorOccurred;

    public VulkanCommandBufferThreadExecutor(Supplier<T> threadDataSupplier) {
        this(SystemInfo.processorCount(), threadDataSupplier);
    }

    public VulkanCommandBufferThreadExecutor(int threadCount, Supplier<T> threadDataSupplier) {
        this.threadCount = assertThat(threadCount, threadCount > 0);
        commandBufferThreads = createCommandBufferThreads(requireNonNull(threadDataSupplier));
        createCommandBufferPointers();
    }

    public void updateUniformsOnly(int count, BiConsumer<Integer, VulkanThreadData> uniformUpdater) {

        final int commandBufferThreadCount = threadCount;
        final int objectsPerCommandBufferThread = min(round((float)count / commandBufferThreadCount), count) + 1;

        if(count <= objectsPerCommandBufferThread) {
            updateUniformsInThisThread(count, uniformUpdater);
        } else {
            updateUniformsInParallel(commandBufferThreadCount, objectsPerCommandBufferThread, count, uniformUpdater);
        }

    }

    private void updateUniformsInThisThread(int count, BiConsumer<Integer, VulkanThreadData> uniformUpdater) {

        final VulkanCommandBufferThread<T> commandBufferThread = commandBufferThreads[0];

        for(int j = 0; j < count; j++) {
            uniformUpdater.accept(j, commandBufferThread.threadData());
        }
    }

    private void updateUniformsInParallel(int commandBufferThreadCount, int objectsPerCommandBufferThread, int count,
                                          BiConsumer<Integer, VulkanThreadData> uniformUpdater) {

        final VulkanCommandBufferThread<T>[] commandBufferThreads = this.commandBufferThreads;
        final CountDownLatch countDownLatch = new CountDownLatch(commandBufferThreadCount);

        for(int i = 0;i < commandBufferThreadCount;i++) {

            final int offset = i * objectsPerCommandBufferThread;
            final int objectCount = min(objectsPerCommandBufferThread, count - offset);

            final VulkanCommandBufferThread<T> commandBufferThread = commandBufferThreads[i];

            commandBufferThread.submit(() -> {

                try {

                    for(int j = 0; j < objectCount; j++) {
                        uniformUpdater.accept(j + offset, commandBufferThread.threadData());
                    }

                } catch(Throwable error) {
                    errorOccurred = true;
                    commandBufferThread.error(error);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Log.fatal("Interrupted exception while waiting for command buffers to be recorded", e);
        }

        if(errorOccurred) {
            logCommandBufferThreadErrors(commandBufferThreadCount);
            errorOccurred = false;
        }
    }

    public void recordCommandBuffers(int count, VkCommandBuffer primaryCommandBuffer, VulkanCommandBufferRecorder<T> recorder) {

        final int commandBufferThreadCount = threadCount;
        final int objectsPerCommandBufferThread = min(round((float)count / commandBufferThreadCount), count) + 1;

        if(count <= objectsPerCommandBufferThread) {
            recordCommandBuffersInThisThread(count, primaryCommandBuffer, recorder);
        } else {
            recordCommandBuffersInParallel(commandBufferThreadCount, objectsPerCommandBufferThread, count, primaryCommandBuffer, recorder);
        }
    }

    private void recordCommandBuffersInThisThread(int count, VkCommandBuffer primaryCommandBuffer, VulkanCommandBufferRecorder<T> recorder) {

        final VulkanCommandBufferThread<T> commandBufferThread = commandBufferThreads[0];

        recordCommandBuffer(0, count, commandBufferThread, recorder);

        vkCmdExecuteCommands(primaryCommandBuffer, commandBufferThread.commandBuffer());
    }

    private void recordCommandBuffersInParallel(int commandBufferThreadCount, int objectsPerCommandBufferThread, int count,
                                                VkCommandBuffer primaryCommandBuffer, VulkanCommandBufferRecorder<T> recorder) {

        final VulkanCommandBufferThread<T>[] commandBufferThreads = this.commandBufferThreads;
        final PointerBuffer pCommandBuffers = this.pCommandBuffers[VulkanRenderer.get().currentSwapchainImageIndex()];
        final CountDownLatch countDownLatch = new CountDownLatch(commandBufferThreadCount);

        for(int i = 0;i < commandBufferThreadCount;i++) {

            final int offset = i * objectsPerCommandBufferThread;
            final int objectCount = min(objectsPerCommandBufferThread, count - offset);

            final VulkanCommandBufferThread<T> commandBufferThread = commandBufferThreads[i];

            commandBufferThread.submit(() -> {

                try {
                    recordCommandBuffer(offset, objectCount, commandBufferThread, recorder);
                } catch(Throwable error) {
                    errorOccurred = true;
                    commandBufferThread.error(error);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Log.fatal("Interrupted exception while waiting for command buffers to be recorded", e);
        }

        if(errorOccurred) {
            logCommandBufferThreadErrors(commandBufferThreadCount);
            errorOccurred = false;
        }

        vkCmdExecuteCommands(primaryCommandBuffer, pCommandBuffers);
    }

    private void logCommandBufferThreadErrors(int commandBufferThreadCount) {

        for(int i = 0;i < commandBufferThreadCount;i++) {

            VulkanCommandBufferThread<T> commandBufferThread = commandBufferThreads[i];

            if(commandBufferThread.error() != null) {
                Log.error("Error while recording command buffer thread " + i, commandBufferThread.error());
                commandBufferThread.error(null);
            }
        }

        Log.fatal("Failed to record Vulkan CommandBuffers");
    }

    private void recordCommandBuffer(int offset, int objectCount, VulkanCommandBufferThread<T> commandBufferThread,
                                     VulkanCommandBufferRecorder<T> recorder) {

        final VkCommandBuffer commandBuffer = commandBufferThread.commandBuffer();
        final T threadData = commandBufferThread.threadData();

        recorder.beginCommandBuffer(commandBuffer, threadData);

        for (int j = 0; j < objectCount; j++) {
            recorder.recordCommandBuffer(j + offset, commandBuffer, threadData);
        }

        recorder.endCommandBuffer(commandBuffer, threadData);

        threadData.end();
    }

    @Override
    public boolean released() {
        return false; // Not used
    }

    @Override
    public void release() {

        Arrays.stream(commandBufferThreads).parallel().forEach(VulkanCommandBufferThread::release);
        Arrays.stream(pCommandBuffers).parallel().forEach(MemoryUtil::memFree);

        commandBufferThreads = null;
        pCommandBuffers = null;
    }

    private void createCommandBufferPointers() {

        pCommandBuffers = new PointerBuffer[Graphics.vulkan().swapchain().imageCount()];

        range(0, pCommandBuffers.length).parallel().forEach(i -> {

            PointerBuffer pointerBuffer = pCommandBuffers[i] = memAllocPointer(threadCount);

            for(int j = 0;j < pointerBuffer.capacity();j++) {
                pointerBuffer.put(j, commandBufferThreads[j].commandBuffer(i));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private VulkanCommandBufferThread<T>[] createCommandBufferThreads(Supplier<T> threadDataSupplier) {

        VulkanCommandBufferThread[] commandBufferThreads = new VulkanCommandBufferThread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            commandBufferThreads[i] = new VulkanCommandBufferThread<>(threadDataSupplier.get());
        }

        return (VulkanCommandBufferThread<T>[]) commandBufferThreads;
    }
}
