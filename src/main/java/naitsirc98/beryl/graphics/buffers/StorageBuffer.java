package naitsirc98.beryl.graphics.buffers;

import naitsirc98.beryl.graphics.GraphicsFactory;

public interface StorageBuffer extends GraphicsMappableBuffer {

    static StorageBuffer create() {
        return GraphicsFactory.get().newStorageBuffer();
    }

}
