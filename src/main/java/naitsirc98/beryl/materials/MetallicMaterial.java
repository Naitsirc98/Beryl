package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.types.ByteSize;

@ByteSize.Static(IMaterial.SIZEOF)
public interface MetallicMaterial extends IMaterial {

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
