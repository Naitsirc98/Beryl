package naitsirc98.beryl.graphics.buffers;

import org.lwjgl.PointerBuffer;

public interface GraphicsCPUBuffer extends GraphicsBuffer {

    PointerBuffer mapMemory(long offset);

    void unmapMemory();

}
