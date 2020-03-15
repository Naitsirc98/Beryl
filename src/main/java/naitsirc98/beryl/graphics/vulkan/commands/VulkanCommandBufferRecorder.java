package naitsirc98.beryl.graphics.vulkan.commands;

import org.lwjgl.vulkan.VkCommandBuffer;

public interface VulkanCommandBufferRecorder<T extends VulkanThreadData> {

    void beginCommandBuffer(VkCommandBuffer commandBuffer);

    void recordCommandBuffer(int index, VkCommandBuffer commandBuffer, T threadData);

    void endCommandBuffer(VkCommandBuffer commandBuffer, T threadData);
}
