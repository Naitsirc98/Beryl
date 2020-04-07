package naitsirc98.beryl.graphics.buffers;

import org.lwjgl.PointerBuffer;

public interface GraphicsMappableBuffer extends GraphicsBuffer {

    default long mapMemoryPtr(long offset) {
        return mapMemory(offset).get(0);
    }

    PointerBuffer mapMemory(long offset);

    void unmapMemory();

}
