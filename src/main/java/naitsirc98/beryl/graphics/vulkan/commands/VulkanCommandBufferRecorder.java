package naitsirc98.beryl.graphics.vulkan.commands;

import org.joml.Matrix4f;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.nio.ByteBuffer;

public interface VulkanCommandBufferRecorder {

    void beginCommandBuffer(VkCommandBuffer commandBuffer);

    void recordCommandBuffer(int index, VkCommandBuffer commandBuffer, ByteBuffer pushConstantData, Matrix4f matrix);

    void endCommandBuffer(VkCommandBuffer commandBuffer);

}
