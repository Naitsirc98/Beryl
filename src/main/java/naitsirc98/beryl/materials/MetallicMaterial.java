package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.types.ByteSize;

import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;

@ByteSize.Static(MetallicMaterial.SIZEOF)
public interface MetallicMaterial extends IMaterial {

    int SIZEOF = 2 * Color.SIZEOF + 4 * FLOAT32_SIZEOF + 5 * Texture.SIZEOF;

    Color color();
    Color emissiveColor();

    Texture2D colorMap();
    Texture2D metallicRoughnessMap();
    Texture2D emissiveMap();
    Texture2D occlusionMap();
    Texture2D normalMap();

    float alpha();
    float metallic();
    float roughness();
    float fresnel();
}
