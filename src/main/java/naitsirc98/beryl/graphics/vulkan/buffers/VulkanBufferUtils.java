package naitsirc98.beryl.graphics.vulkan.buffers;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class VulkanBufferUtils {

    public static void transferToGPUBuffer(long offset, ByteBuffer data, VulkanGPUBuffer buffer) {
        try(VulkanStagingBuffer stagingBuffer = new VulkanStagingBuffer(data)) {
            stagingBuffer.transfer(offset, buffer);
        }
    }

    public static void transferToGPUBuffer(long offset, IntBuffer data, VulkanGPUBuffer buffer) {
        try(VulkanStagingBuffer stagingBuffer = new VulkanStagingBuffer(data)) {
            stagingBuffer.transfer(offset, buffer);
        }
    }

    public static void transferToGPUBuffer(long offset, FloatBuffer data, VulkanGPUBuffer buffer) {
        try(VulkanStagingBuffer stagingBuffer = new VulkanStagingBuffer(data)) {
            stagingBuffer.transfer(offset, buffer);
        }
    }

}
