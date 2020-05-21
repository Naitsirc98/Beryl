package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.buffers.StorageBuffer;
import naitsirc98.beryl.graphics.textures.Texture;

import java.util.ArrayList;
import java.util.List;

import static naitsirc98.beryl.util.handles.LongHandle.NULL;

public abstract class MaterialStorageHandler<T extends ManagedMaterial> {

    private List<T> materials;
    private StorageBuffer buffer;
    private long offset;

    public MaterialStorageHandler() {
        materials = new ArrayList<>();
        buffer = createStorageBuffer();
    }

    public void allocate(T material) {

        checkBuffer();

        storeMaterialToBuffer(material, offset, buffer);

        MaterialStorageInfo storageInfo = material.storageInfo();
        storageInfo.index(materials.size());
        storageInfo.offset(offset);

        offset += getMaterialSizeof();

        materials.add(material);
    }

    public void update(T material) {

        MaterialStorageInfo storageInfo = material.storageInfo();

        storeMaterialToBuffer(material, storageInfo.offset(), buffer);
    }

    public void free(T material) {

        // Free the storage occupied by the given material

        MaterialStorageInfo storageInfo = material.storageInfo();

        final int index = storageInfo.index();

        final int sizeof = getMaterialSizeof();

        long offset = index * sizeof;

        for(int i = index;i < materials.size();i++) {

            final T nextMesh = materials.get(i);

            storeMaterialToBuffer(material, offset, buffer);

            nextMesh.storageInfo().index(i);

            offset += sizeof;
        }

        this.offset = offset;

        materials.remove(index);
    }

    protected void clear() {
        materials.clear();
        buffer.reallocate(getMaterialSizeof());
        offset = 0;
    }

    protected void terminate() {
        materials.clear();
        buffer.release();
        offset = Long.MIN_VALUE;
    }

    protected abstract void storeMaterialToBuffer(T material, long offset, StorageBuffer buffer);

    protected void checkBuffer() {

        final long size = buffer.size();

        if(offset >= size) {
            buffer.resize(size + getMaterialSizeof());
        }
    }

    protected StorageBuffer createStorageBuffer() {
        StorageBuffer buffer = GraphicsFactory.get().newStorageBuffer();
        buffer.allocate(getMaterialSizeof());
        offset = 0;
        return buffer;
    }

    protected abstract int getMaterialSizeof();

    protected long textureResidentHandle(Texture texture) {
        return texture == null ? NULL : texture.makeResident();
    }

    @SuppressWarnings("unchecked")
    public <T extends StorageBuffer> T buffer() {
        return (T) buffer;
    }
}
