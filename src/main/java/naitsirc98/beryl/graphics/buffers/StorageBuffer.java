package naitsirc98.beryl.graphics.buffers;

import naitsirc98.beryl.graphics.GraphicsFactory;

public interface StorageBuffer extends MappedGraphicsBuffer {

    static StorageBuffer create() {
        return GraphicsFactory.get().newStorageBuffer();
    }

}
