package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.buffers.StorageBuffer;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

public class PBRMetallicMaterialStorageHandler extends MaterialStorageHandler<PBRMetallicMaterial> {

    @Override
    protected void storeMaterialToBuffer(PBRMetallicMaterial material, long offset, StorageBuffer buffer) {

        try(MemoryStack stack = MemoryStack.stackPush()) {

            ByteBuffer data = stack.malloc(getMaterialSizeof());

            material.getAlbedo().getRGBA(data);
            material.getEmissiveColor().getRGBA(data);

            data.putLong(textureResidentHandle(material.getAlbedoMap()));
            data.putLong(textureResidentHandle(material.getMetallicMap()));
            data.putLong(textureResidentHandle(material.getRoughnessMap()));
            data.putLong(textureResidentHandle(material.getOcclusionMap()));
            data.putLong(textureResidentHandle(material.getEmissiveMap()));
            data.putLong(textureResidentHandle(material.getNormalMap()));

            data.putFloat(material.tiling().x()).putFloat(material.tiling().y());

            data.putFloat(material.getAlpha());
            data.putFloat(material.getMetallic());
            data.putFloat(material.getRoughness());
            data.putFloat(material.getOcclusion());
            data.putFloat(material.getFresnel0());

            data.putInt(material.flags());

            buffer.update(offset, data.rewind());
        }
    }

    @Override
    protected int getMaterialSizeof() {
        return PBRMetallicMaterial.SIZEOF;
    }
}
