package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.rendering.ShadingModel;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.IColor;
import naitsirc98.beryl.util.types.StaticByteSize;

import static naitsirc98.beryl.util.types.DataType.*;

@StaticByteSize(sizeof = PBRMetallicMaterial.SIZEOF)
public class PBRMetallicMaterial extends ManagedMaterial implements Material {

    public static final int SIZEOF = 2 * VECTOR4_SIZEOF + 6 * SAMPLER_SIZEOF + VECTOR2_SIZEOF + 5 * FLOAT32_SIZEOF + INT32_SIZEOF;

    private static final float DEFAULT_ALPHA = 1.0f;
    private static final float DEFAULT_METALLIC = 0.0f;
    private static final float DEFAULT_ROUGHNESS = 0.0f;
    private static final float DEFAULT_OCCLUSION = 0.0f;
    private static final float DEFAULT_FRESNEL0 = 0.04f;

    // FLAGS
    // private static final int ALBEDO_MAP_PRESENT = 0x1;
    private static final int METALLIC_MAP_PRESENT = 0x2;
    private static final int ROUGHNESS_MAP_PRESENT = 0x4;
    private static final int OCCLUSION_MAP_PRESENT = 0x8;
    private static final int NORMAL_MAP_PRESENT = 0x10;
    // === //

    private static final MaterialFactory<PBRMetallicMaterial> FACTORY = new MaterialFactory<>(PBRMetallicMaterial.class);

    public static MaterialFactory<PBRMetallicMaterial> getFactory() {
        return FACTORY;
    }


    // Colors
    private Color albedo;
    private Color emissiveColor;
    // Textures
    private Texture2D albedoMap;
    private Texture2D metallicMap;
    private Texture2D roughnessMap;
    private Texture2D occlusionMap;
    private Texture2D emissiveMap;
    private Texture2D normalMap;
    // Values
    private float alpha;
    private float metallic;
    private float roughness;
    private float occlusion;
    private float fresnel0;

    public PBRMetallicMaterial(String name) {
        super(name);
        setDefaults();
    }

    private void setDefaults() {

        albedo = Color.colorWhite();
        emissiveColor = Color.colorBlackTransparent();

        albedoMap = WHITE_TEXTURE;
        emissiveMap = BLACK_TEXTURE;

        alpha = DEFAULT_ALPHA;
        metallic = DEFAULT_METALLIC;
        roughness = DEFAULT_ROUGHNESS;
        occlusion = DEFAULT_OCCLUSION;
        fresnel0 = DEFAULT_FRESNEL0;
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    public IColor getAlbedo() {
        return albedo;
    }

    public PBRMetallicMaterial setAlbedo(IColor albedo) {
        this.albedo.set(albedo);
        markModified();
        return this;
    }

    public IColor getEmissiveColor() {
        return emissiveColor;
    }

    public PBRMetallicMaterial setEmissiveColor(IColor emissiveColor) {
        this.emissiveColor.set(emissiveColor);
        markModified();
        return this;
    }

    public Texture2D getAlbedoMap() {
        return getMapOrDefault(albedoMap);
    }

    public PBRMetallicMaterial setAlbedoMap(Texture2D albedoMap) {
        updateTexturesUseCount(this.albedoMap, albedoMap);
        this.albedoMap = albedoMap;
        markModified();
        return this;
    }

    public Texture2D getMetallicMap() {
        return metallicMap;
    }

    public PBRMetallicMaterial setMetallicMap(Texture2D metallicMap) {
        updateTexturesUseCount(this.metallicMap, metallicMap);
        this.metallicMap = metallicMap;
        if(metallicMap != null) {
            setFlag(METALLIC_MAP_PRESENT);
        } else {
            removeFlag(METALLIC_MAP_PRESENT);
        }
        markModified();
        return this;
    }

    public Texture2D getRoughnessMap() {
        return roughnessMap;
    }

    public PBRMetallicMaterial setRoughnessMap(Texture2D roughnessMap) {
        updateTexturesUseCount(this.roughnessMap, roughnessMap);
        this.roughnessMap = roughnessMap;
        if(roughnessMap != null) {
            setFlag(ROUGHNESS_MAP_PRESENT);
        } else {
            removeFlag(ROUGHNESS_MAP_PRESENT);
        }
        markModified();
        return this;
    }

    public Texture2D getOcclusionMap() {
        return occlusionMap;
    }

    public PBRMetallicMaterial setOcclusionMap(Texture2D occlusionMap) {
        updateTexturesUseCount(this.occlusionMap, occlusionMap);
        this.occlusionMap = occlusionMap;
        if(occlusionMap != null) {
            setFlag(OCCLUSION_MAP_PRESENT);
        } else {
            removeFlag(OCCLUSION_MAP_PRESENT);
        }
        markModified();
        return this;
    }

    public Texture2D getEmissiveMap() {
        return getMapOrDefault(emissiveMap);
    }

    public PBRMetallicMaterial setEmissiveMap(Texture2D emissiveMap) {
        updateTexturesUseCount(this.emissiveMap, emissiveMap);
        this.emissiveMap = emissiveMap;
        markModified();
        return this;
    }

    public Texture2D getNormalMap() {
        return normalMap;
    }

    public PBRMetallicMaterial setNormalMap(Texture2D normalMap) {

        updateTexturesUseCount(this.normalMap, normalMap);

        this.normalMap = normalMap;

        if(normalMap != null) {
            setFlag(NORMAL_MAP_PRESENT);
        } else {
            removeFlag(NORMAL_MAP_PRESENT);
        }

        markModified();

        return this;
    }

    public float getAlpha() {
        return alpha;
    }

    public PBRMetallicMaterial setAlpha(float alpha) {
        this.alpha = alpha;
        return this;
    }

    public float getMetallic() {
        return metallic;
    }

    public PBRMetallicMaterial setMetallic(float metallic) {
        this.metallic = metallic;
        return this;
    }

    public float getRoughness() {
        return roughness;
    }

    public PBRMetallicMaterial setRoughness(float roughness) {
        this.roughness = roughness;
        return this;
    }

    public float getOcclusion() {
        return occlusion;
    }

    public PBRMetallicMaterial setOcclusion(float occlusion) {
        this.occlusion = occlusion;
        return this;
    }

    public float getFresnel0() {
        return fresnel0;
    }

    public PBRMetallicMaterial setFresnel0(float fresnel0) {
        this.fresnel0 = fresnel0;
        return this;
    }

    @Override
    public ShadingModel shadingModel() {
        return ShadingModel.PBR_METALLIC;
    }
}
