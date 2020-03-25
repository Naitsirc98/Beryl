package naitsirc98.beryl.graphics.vulkan.rendering.phong;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.graphics.vulkan.commands.VulkanThreadData;
import naitsirc98.beryl.graphics.vulkan.vertex.VulkanVertexData;
import org.joml.Matrix4f;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static naitsirc98.beryl.graphics.vulkan.rendering.phong.VulkanPhongRenderingPath.MATRICES_UNIFORM_BUFFER_SIZE;
import static naitsirc98.beryl.util.types.DataType.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;

final class VulkanPhongThreadData implements VulkanThreadData, VulkanObject {

    static final int CAMERA_POSITION_PUSH_CONSTANT_SIZE = 4 * FLOAT32_SIZEOF;
    static final int MVP_PUSH_CONSTANT_SIZE =  4 * 4 * FLOAT32_SIZEOF;
    static final int PUSH_CONSTANT_SIZE = CAMERA_POSITION_PUSH_CONSTANT_SIZE + MVP_PUSH_CONSTANT_SIZE;

    private static final int THREAD_DATA_BUFFER_SIZE = 3 * INT64_SIZEOF + 2 * INT32_SIZEOF + PUSH_CONSTANT_SIZE + MATRICES_UNIFORM_BUFFER_SIZE;

    private final ByteBuffer buffer;

    private final LongBuffer pDescriptorSets;
    private final IntBuffer pDynamicOffsets;

    final Matrix4f matrix;
    final ByteBuffer pushConstantData;
    final long pushConstantDataAddress;
    final ByteBuffer matricesData;
    final long matricesDataAddress;

    VulkanVertexData lastVertexData;

    public VulkanPhongThreadData() {

        buffer = memAlloc(THREAD_DATA_BUFFER_SIZE);

        final long bufferAddress = memAddress(buffer);

        pDescriptorSets = memLongBuffer(bufferAddress, 3);
        pDynamicOffsets = memIntBuffer(memAddress(pDescriptorSets) + 3 * INT64_SIZEOF, 2);

        matrix = new Matrix4f();

        pushConstantData = memByteBuffer(memAddress(pDynamicOffsets) + 2 * INT32_SIZEOF, PUSH_CONSTANT_SIZE);
        pushConstantDataAddress = memAddress(pushConstantData);

        matricesData = memByteBuffer(memAddress(pushConstantData) + PUSH_CONSTANT_SIZE, MATRICES_UNIFORM_BUFFER_SIZE);
        matricesDataAddress = memAddress(matricesData);
    }

    @Override
    public void end() {
        lastVertexData = null;
    }

    @Override
    public void release() {
        memFree(buffer);
    }

    void bindDescriptorSets(VkCommandBuffer commandBuffer, long pipelineLayout) {
        vkCmdBindDescriptorSets(
                commandBuffer,
                VK_PIPELINE_BIND_POINT_GRAPHICS,
                pipelineLayout,
                0,
                pDescriptorSets,
                pDynamicOffsets);
    }

    void updateUniformInfo(int matricesOffset, int materialOffset,
                           long matricesDescriptorSet, long materialDescriptorSet, long lightsDescriptorSet) {

        pDescriptorSets.put(0, matricesDescriptorSet).put(1, materialDescriptorSet).put(2, lightsDescriptorSet);
        pDynamicOffsets.put(0, matricesOffset).put(1, materialOffset);
    }

}