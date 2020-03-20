package naitsirc98.beryl.graphics.vulkan.rendering.simple;

import naitsirc98.beryl.graphics.vulkan.commands.VulkanThreadData;
import naitsirc98.beryl.graphics.vulkan.vertex.VulkanVertexData;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;

import static naitsirc98.beryl.util.types.DataType.FLOAT32;
import static org.lwjgl.system.MemoryUtil.*;

final class VulkanSimpleThreadData implements VulkanThreadData {

    static final int PUSH_CONSTANT_DATA_SIZE = 4 * 4 * FLOAT32.sizeof();

    final Matrix4f matrix;
    final ByteBuffer pushConstantData;
    final long pushConstantDataAddress;
    VulkanVertexData lastVertexData;

    public VulkanSimpleThreadData() {
        matrix = new Matrix4f();
        pushConstantData = memAlloc(PUSH_CONSTANT_DATA_SIZE);
        pushConstantDataAddress = memAddress(pushConstantData);
    }

    @Override
    public void release() {
        memFree(pushConstantData);
    }

    @Override
    public void end() {
        lastVertexData = null;
    }
}
