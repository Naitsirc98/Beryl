package naitsirc98.beryl.materials;

import naitsirc98.beryl.assets.Asset;
import naitsirc98.beryl.util.BitFlags;
import naitsirc98.beryl.util.types.ByteSize;

public interface IMaterial extends Asset, ByteSize {

    int PHONG_MATERIAL_BIT = 0x80;
    int METALLIC_MATERIAL_BIT = 0x100;
    int SPECULAR_MATERIAL_BIT = 0x200;

    // Colors
    byte AMBIENT_COLOR = -1;
    byte DIFFUSE_COLOR = -2;
    byte SPECULAR_COLOR = -3;
    byte EMISSIVE_COLOR = -4;
    byte COLOR = DIFFUSE_COLOR;

    // Textures
    byte AMBIENT_MAP = 0x1;
    byte DIFFUSE_MAP = 0x2;
    byte SPECULAR_MAP = 0x4;
    byte EMISSIVE_MAP = 0x8;
    byte COLOR_MAP = DIFFUSE_MAP;
    byte NORMAL_MAP = 0x10;
    byte METALLIC_ROUGHNESS_MAP = 0x20;
    byte SPECULAR_GLOSSINESS_MAP = METALLIC_ROUGHNESS_MAP;
    byte OCCLUSION_MAP = 0x40;

    // Float values
    byte ALPHA = -5;
    byte SHININESS = -6;
    byte FRESNEL = -7;
    byte REFLECTIVITY = -8;
    byte REFRACTIVE_INDEX = -9;
    byte GLOSSINESS = -10;
    byte METALLIC = -11;
    byte ROUGHNESS = -12;


    int type();

    long offset();

    int bufferIndex();

    int index();

    BitFlags flags();

    boolean destroyed();
}
