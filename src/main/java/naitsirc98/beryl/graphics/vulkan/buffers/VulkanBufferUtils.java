package naitsirc98.beryl.graphics.vulkan.buffers;

import naitsirc98.beryl.graphics.vulkan.textures.VulkanImage;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class VulkanBufferUtils {

    public static void transferToImage(int mipLevel, int xOffset, int yOffset, int zOffset, ByteBuffer pixels, VulkanImage image) {
        try(VulkanStagingBuffer stagingBuffer = new VulkanStagingBuffer(pixels)) {
            stagingBuffer.transfer(mipLevel, xOffset, yOffset, zOffset, image);
        }
    }

    public static void transferToImage(int mipLevel, int xOffset, int yOffset, int zOffset, FloatBuffer pixels, VulkanImage image) {
        try(VulkanStagingBuffer stagingBuffer = new VulkanStagingBuffer(pixels)) {
            stagingBuffer.transfer(mipLevel, xOffset, yOffset, zOffset, image);
        }
    }

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
