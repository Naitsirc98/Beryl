package naitsirc98.beryl.graphics.vulkan.commands;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.vulkan.rendering.VulkanRenderer;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.SystemInfo;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.range;
import static naitsirc98.beryl.util.Asserts.assertThat;
import static org.joml.Math.min;
import static org.joml.Math.round;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.vulkan.VK10.vkCmdExecuteCommands;

public class VulkanCommandBufferThreadExecutor<T extends VulkanThreadData> implements NativeResource {

    private VulkanCommandBufferThread<T>[] commandBufferThreads;
    private PointerBuffer[] pCommandBuffers;
    private final int threadCount;

    public VulkanCommandBufferThreadExecutor(Supplier<T> threadDataSupplier) {
        this(SystemInfo.processorCount(), threadDataSupplier);
    }

    public VulkanCommandBufferThreadExecutor(int threadCount, Supplier<T> threadDataSupplier) {
        this.threadCount = assertThat(threadCount, threadCount > 0);
        commandBufferThreads = createCommandBufferThreads(requireNonNull(threadDataSupplier));
        createCommandBufferPointers();
    }

    public void recordCommandBuffers(int count, VkCommandBuffer primaryCommandBuffer, VulkanCommandBufferRecorder<T> recorder) {

        final int commandBuilderCount = threadCount;
        final int objectsPerCommandBuilder = min(round((float)count / commandBuilderCount), count);

        if(count <= objectsPerCommandBuilder) {
            recordCommandBuffersInThisThread(count, primaryCommandBuffer, recorder);
        } else {
            recordCommandBuffersInParallel(commandBuilderCount, objectsPerCommandBuilder, count, primaryCommandBuffer, recorder);
        }
    }

    private void recordCommandBuffersInThisThread(int count, VkCommandBuffer primaryCommandBuffer, VulkanCommandBufferRecorder<T> recorder) {

        final VulkanCommandBufferThread<T> commandBufferThread = commandBufferThreads[0];

        recordCommandBuffer(0, count, commandBufferThread, recorder);

        vkCmdExecuteCommands(primaryCommandBuffer, commandBufferThread.commandBuffer());
    }

    private void recordCommandBuffersInParallel(int commandBuilderCount, int objectsPerCommandBuilder, int count,
                                                VkCommandBuffer primaryCommandBuffer, VulkanCommandBufferRecorder<T> recorder) {

        final VulkanCommandBufferThread<T>[] commandBufferThreads = this.commandBufferThreads;
        final PointerBuffer pCommandBuffers = this.pCommandBuffers[VulkanRenderer.get().currentSwapchainImageIndex()];
        final CountDownLatch countDownLatch = new CountDownLatch(commandBuilderCount);

        range(0, commandBuilderCount).unordered().parallel().forEach(i -> {

            final int offset = i * objectsPerCommandBuilder;
            final int objectCount = min(objectsPerCommandBuilder, count - offset);

            final VulkanCommandBufferThread<T> commandBufferThread = commandBufferThreads[i];

            commandBufferThread.submit(() -> {

                recordCommandBuffer(offset, objectCount, commandBufferThread, recorder);

                countDownLatch.countDown();
            });
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Log.error("Interrupted exception while waiting for command buffers to be recorded", e);
        }

        vkCmdExecuteCommands(primaryCommandBuffer, pCommandBuffers);
    }

    private void recordCommandBuffer(int offset, int objectCount, VulkanCommandBufferThread<T> commandBufferThread,
                                     VulkanCommandBufferRecorder<T> recorder) {

        final VkCommandBuffer commandBuffer = commandBufferThread.commandBuffer();
        final T threadData = commandBufferThread.threadData();

        recorder.beginCommandBuffer(commandBuffer);

        for (int j = 0; j < objectCount; j++) {
            recorder.recordCommandBuffer(j + offset, commandBuffer, threadData);
        }

        recorder.endCommandBuffer(commandBuffer);
    }

    @Override
    public void free() {

        Arrays.stream(commandBufferThreads).parallel().forEach(VulkanCommandBufferThread::free);
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
