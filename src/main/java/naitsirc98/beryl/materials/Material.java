package naitsirc98.beryl.materials;

import naitsirc98.beryl.assets.Asset;
import naitsirc98.beryl.util.types.ByteSize;
import org.joml.Vector2fc;

public interface Material extends Asset, ByteSize {

    Vector2fc tiling();

    Material tiling(float x, float y);

    MaterialType type();

    int flags();

    boolean destroyed();

}
