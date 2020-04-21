package naitsirc98.beryl.materials;

import naitsirc98.beryl.assets.Asset;
import naitsirc98.beryl.util.types.ByteSize;
import org.joml.Vector2f;
import org.joml.Vector2fc;

@ByteSize.Static(IMaterial.SIZEOF)
public interface IMaterial extends Asset, ByteSize {

    // int SIZEOF = roundUp2(4 * Color.SIZEOF + 6 * UINT64_SIZEOF + VECTOR2_SIZEOF + 4 * FLOAT32_SIZEOF, VECTOR4_SIZEOF);

    int SIZEOF = 144;

    // Colors
    byte AMBIENT_COLOR = 0;
    byte DIFFUSE_COLOR = 1;
    byte SPECULAR_COLOR = 2;
    byte EMISSIVE_COLOR = 3;
    byte COLOR = DIFFUSE_COLOR;

    // Textures
    byte AMBIENT_MAP = 4;
    byte DIFFUSE_MAP = 5;
    byte SPECULAR_MAP = 6;
    byte EMISSIVE_MAP = 7;
    byte COLOR_MAP = DIFFUSE_MAP;
    byte NORMAL_MAP = 8;
    byte METALLIC_ROUGHNESS_MAP = 9;
    byte SPECULAR_GLOSSINESS_MAP = METALLIC_ROUGHNESS_MAP;
    byte OCCLUSION_MAP = 10;

    byte TEXTURE_TILING = 11;

    // Float values
    byte ALPHA = 12;
    byte SHININESS = 13;
    byte FRESNEL = 14;
    byte REFLECTIVITY = 15;
    byte REFRACTIVE_INDEX = 16;
    byte GLOSSINESS = 17;
    byte METALLIC = 18;
    byte ROUGHNESS = 19;

    byte REFLECTION_MAP = 20;
    byte REFRACTION_MAP = 21;
    byte DUDV_MAP = 22;

    // === FLAGS === //

    int NORMAL_MAP_PRESENT = 0x1;


    Vector2fc DEFAULT_TEXTURE_TILING = new Vector2f(1.0f, 1.0f);


    Vector2fc textureTiling();

    Material textureTiling(float x, float y);

    Type type();

    long offset();

    int bufferIndex();

    int index();

    int flags();

    void modify();

    boolean modified();

    boolean destroyed();


    enum Type {
        PHONG_MATERIAL,
        METALLIC_MATERIAL,
        SPECULAR_MATERIAL,
        WATER_MATERIAL
    }
}
