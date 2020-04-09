package naitsirc98.beryl.graphics.buffers;

import org.lwjgl.PointerBuffer;

public interface GraphicsMappableBuffer extends GraphicsBuffer {

    default long mappedMemory() {
        return mapMemoryPtr(0);
    }

    default long mapMemoryPtr(long offset) {
        return mapMemory(offset).get(0);
    }

    default PointerBuffer mapMemory() {
        return mapMemory(0);
    }

    PointerBuffer mapMemory(long offset);

    void flush();

    void unmapMemory();

}
