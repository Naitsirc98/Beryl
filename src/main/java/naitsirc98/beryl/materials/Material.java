package naitsirc98.beryl.materials;

import naitsirc98.beryl.assets.Asset;
import naitsirc98.beryl.util.types.ByteSize;
import org.joml.Vector2fc;

public interface Material extends Asset, ByteSize {

    Vector2fc tiling();

    Material tiling(float x, float y);

    Material.Type type();

    int flags();

    boolean destroyed();

    enum Type {

        PHONG_MATERIAL,
        PBR_METALLIC_MATERIAL,
        WATER_MATERIAL
    }

}
