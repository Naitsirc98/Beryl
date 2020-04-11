package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.types.ByteSize;

@ByteSize.Static(IMaterial.SIZEOF)
public interface SpecularMaterial extends IMaterial {

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
