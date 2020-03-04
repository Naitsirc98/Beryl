package naitsirc98.beryl.graphics.vulkan.commands;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.logging.Log;
import org.joml.Matrix4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import static org.joml.Math.min;
import static org.joml.Math.round;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.vkCmdExecuteCommands;

public class VulkanCommandBuilderExecutor implements NativeResource {

    private static final int COMMAND_BUILDER_COUNT = 32;

    private List<VulkanCommandBuilder> commandBuilders;
    private List<Matrix4f> matrices;
    private PointerBuffer[] pCommandBuffers;

    public VulkanCommandBuilderExecutor() {
        commandBuilders = createCommandBuilders();
        matrices = createMatrices();
        createCommandBufferPointers();
    }

    public void recordCommandBuffers(int count, VkCommandBuffer primaryCommandBuffer, VulkanCommandBufferRecorder recorder) {

        final List<VulkanCommandBuilder> commandBuilders = this.commandBuilders;
        final int commandBuilderCount = commandBuilders.size();
        final PointerBuffer pCommandBuffers = this.pCommandBuffers[Graphics.vulkan().renderer().currentSwapchainImageIndex()];
        final CountDownLatch countDownLatch = new CountDownLatch(commandBuilderCount);
        final int objectsPerCommandBuilder = min(round((float)count / commandBuilderCount), count);

        IntStream.range(0, commandBuilderCount).parallel().forEach(i -> {

            final int offset = i * objectsPerCommandBuilder;
            final int objectCount = min(objectsPerCommandBuilder, count - offset);

            final VulkanCommandBuilder commandBuilder = commandBuilders.get(i);
            final Matrix4f matrix = matrices.get(i);

            commandBuilder.submitRecordTask(() -> {

                final VkCommandBuffer commandBuffer = commandBuilder.commandBuffer();
                final ByteBuffer pushConstantData = commandBuilder.pushConstantData();

                recorder.beginCommandBuffer(commandBuffer);

                for (int j = 0; j < objectCount; j++) {
                    recorder.recordCommandBuffer(j + offset, commandBuffer, pushConstantData, matrix);
                }

                recorder.endCommandBuffer(commandBuffer);

                countDownLatch.countDown();
            });
        });

        // commandBuilders.forEach(VulkanCommandBuilder::await);

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Log.error("Interrupted exception while waiting for command buffers to be recorded", e);
        }

        vkCmdExecuteCommands(primaryCommandBuffer, pCommandBuffers);
    }

    @Override
    public void free() {

        commandBuilders.forEach(VulkanCommandBuilder::free);
        commandBuilders.clear();
        commandBuilders = null;

        for(PointerBuffer pointerBuffer : pCommandBuffers) {
            memFree(pointerBuffer);
        }
        pCommandBuffers = null;

        matrices.clear();
        matrices = null;
    }

    private List<Matrix4f> createMatrices() {
        List<Matrix4f> matrices = new ArrayList<>(commandBuilders.size());
        for(int i = 0;i < commandBuilders.size();i++) {
            matrices.add(new Matrix4f());
        }
        return matrices;
    }

    private void createCommandBufferPointers() {

        pCommandBuffers = new PointerBuffer[Graphics.vulkan().swapchain().swapChainImages().length];

        for(int i = 0;i < pCommandBuffers.length;i++) {

            PointerBuffer pointerBuffer = pCommandBuffers[i] = memAllocPointer(commandBuilders.size());

            for(int j = 0;j < pointerBuffer.capacity();j++) {
                pointerBuffer.put(j, commandBuilders.get(j).commandBuffer(i));
            }
        }
    }

    private List<VulkanCommandBuilder> createCommandBuilders() {
        List<VulkanCommandBuilder> commandBuilders = new ArrayList<>(COMMAND_BUILDER_COUNT);
        for(int i = 0;i < COMMAND_BUILDER_COUNT;i++) {
            commandBuilders.add(new VulkanCommandBuilder());
        }
        return commandBuilders;
    }


}
