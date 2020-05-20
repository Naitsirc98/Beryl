package naitsirc98.beryl.materials.v2;

import naitsirc98.beryl.assets.Asset;
import naitsirc98.beryl.util.types.ByteSize;
import org.joml.Vector2fc;

public interface Material extends Asset, ByteSize {

    Vector2fc tiling();

    Material tiling(float x, float y);

    Material.Type type();

    MaterialStorageInfo storageInfo();

    int flags();

    boolean modified();

    boolean destroyed();

    enum Type {

        PHONG_MATERIAL,
        PBR_METALLIC_MATERIAL,
        WATER_MATERIAL
    }

}
