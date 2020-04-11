package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.types.ByteSize;

import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;

@ByteSize.Static(SpecularMaterial.SIZEOF)
public interface SpecularMaterial extends IMaterial {

    int SIZEOF = 3 * Color.SIZEOF + 3 * FLOAT32_SIZEOF + 5 * Texture.SIZEOF;

    Color diffuseColor();
    Color specularColor();
    Color emissiveColor();

    Texture2D diffuseMap();
    Texture2D specularGlossinessMap();
    Texture2D emissiveMap();
    Texture2D occlusionMap();
    Texture2D normalMap();

    float alpha();
    float glossiness();
    float fresnel();
}
