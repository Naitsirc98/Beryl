package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.buffers.StorageBuffer;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

public class PBRMetallicMaterialStorageHandler extends MaterialStorageHandler<PBRMetallicMaterial> {

    @Override
    protected void storeMaterialToBuffer(PBRMetallicMaterial material, long offset, StorageBuffer buffer) {

        try(MemoryStack stack = MemoryStack.stackPush()) {

            ByteBuffer data = stack.malloc(getMaterialSizeof());

            material.albedo().getRGBA(data);
            material.emissiveColor().getRGBA(data);

            data.putLong(textureResidentHandle(material.albedoMap()));
            data.putLong(textureResidentHandle(material.metallicMap()));
            data.putLong(textureResidentHandle(material.roughnessMap()));
            data.putLong(textureResidentHandle(material.occlusionMap()));
            data.putLong(textureResidentHandle(material.emissiveMap()));
            data.putLong(textureResidentHandle(material.normalMap()));

            data.putFloat(material.tiling().x()).putFloat(material.tiling().y());

            data.putFloat(material.alpha());
            data.putFloat(material.metallic());
            data.putFloat(material.roughness());
            data.putFloat(material.occlusion());
            data.putFloat(material.fresnel0());

            data.putInt(material.flags());

            buffer.update(offset, data.rewind());
        }
    }

    @Override
    protected int getMaterialSizeof() {
        return PBRMetallicMaterial.SIZEOF;
    }
}
