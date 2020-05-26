package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.buffers.StorageBuffer;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

public class PhongMaterialStorageHandler extends MaterialStorageHandler<PhongMaterial> {

    PhongMaterialStorageHandler() {
    }

    @Override
    protected void storeMaterialToBuffer(PhongMaterial material, long offset, StorageBuffer buffer) {

        try(MemoryStack stack = MemoryStack.stackPush()) {

            ByteBuffer data = stack.malloc(getMaterialSizeof());

            material.ambientColor().getRGBA(data);
            material.diffuseColor().getRGBA(data);
            material.specularColor().getRGBA(data);
            material.emissiveColor().getRGBA(data);

            data.putLong(textureResidentHandle(material.ambientMap()));
            data.putLong(textureResidentHandle(material.diffuseMap()));
            data.putLong(textureResidentHandle(material.specularMap()));
            data.putLong(textureResidentHandle(material.emissiveMap()));
            data.putLong(textureResidentHandle(material.occlusionMap()));
            data.putLong(textureResidentHandle(material.normalMap()));

            data.putFloat(material.tiling().x()).putFloat(material.tiling().y());

            data.putFloat(material.alpha());
            data.putFloat(material.shininess());
            data.putFloat(material.reflectivity());
            data.putFloat(material.refractiveIndex());

            data.putInt(material.flags());

            buffer.update(offset, data.rewind());
        }
    }

    @Override
    protected int getMaterialSizeof() {
        return PhongMaterial.SIZEOF;
    }
}
