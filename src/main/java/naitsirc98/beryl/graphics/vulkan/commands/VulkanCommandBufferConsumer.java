package naitsirc98.beryl.graphics.vulkan.commands;

import org.lwjgl.vulkan.VkCommandBuffer;

import java.nio.ByteBuffer;

@FunctionalInterface
public interface VulkanCommandBufferConsumer {

    void consume(int index, VkCommandBuffer commandBuffer, ByteBuffer pushConstantData);

}
