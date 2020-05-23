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

            material.getAmbientColor().getRGBA(data);
            material.getDiffuseColor().getRGBA(data);
            material.getSpecularColor().getRGBA(data);
            material.getEmissiveColor().getRGBA(data);

            data.putLong(textureResidentHandle(material.getAmbientMap()));
            data.putLong(textureResidentHandle(material.getDiffuseMap()));
            data.putLong(textureResidentHandle(material.getSpecularMap()));
            data.putLong(textureResidentHandle(material.getEmissiveMap()));
            data.putLong(textureResidentHandle(material.getOcclusionMap()));
            data.putLong(textureResidentHandle(material.getNormalMap()));

            data.putFloat(material.tiling().x()).putFloat(material.tiling().y());

            data.putFloat(material.getAlpha());
            data.putFloat(material.getShininess());
            data.putFloat(material.getReflectivity());
            data.putFloat(material.getRefractiveIndex());

            data.putInt(material.flags());

            buffer.update(offset, data.rewind());
        }
    }

    @Override
    protected int getMaterialSizeof() {
        return PhongMaterial.SIZEOF;
    }
}
