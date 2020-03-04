package naitsirc98.beryl.graphics.vulkan.commands;

import naitsirc98.beryl.graphics.Graphics;
import org.joml.Matrix4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static naitsirc98.beryl.util.Asserts.assertTrue;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.vkCmdExecuteCommands;

public class VulkanCommandBuilderExecutor implements NativeResource {

    private List<VulkanCommandBuilder> commandBuilders;
    private List<Matrix4f> matrices;
    private PointerBuffer[] pCommandBuffers;

    public VulkanCommandBuilderExecutor() {
        commandBuilders = createCommandBuilders();
        matrices = createMatrices();
        createCommandBufferPointers();
    }

    public void recordCommandBuffers(int count, VkCommandBuffer primaryCommandBuffer, VulkanCommandBufferRecorder recorder) {

        List<VulkanCommandBuilder> commandBuilders = this.commandBuilders;
        final int commandBuilderCount = commandBuilders.size();
        PointerBuffer pCommandBuffers = this.pCommandBuffers[Graphics.vulkan().renderer().currentSwapchainImageIndex()];

        int remaining = count;
        int objectsPerCommandBuilder = Math.max(Math.round((float)count / commandBuilderCount), count);

        for(int i = 0; i < commandBuilderCount; i++) {

            final int objectCount = Math.min(remaining, objectsPerCommandBuilder);
            final int offset = count - remaining;

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
            });

            remaining -= objectCount;
        }

        assertTrue(remaining == 0);

        commandBuilders.forEach(VulkanCommandBuilder::await);

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
        List<VulkanCommandBuilder> commandBuilders = new ArrayList<>(4);
        for(int i = 0;i < 4;i++) {
            commandBuilders.add(new VulkanCommandBuilder());
        }
        return commandBuilders;
    }


}
