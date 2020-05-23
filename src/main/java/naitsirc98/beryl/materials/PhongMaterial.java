package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.rendering.ShadingModel;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.IColor;
import naitsirc98.beryl.util.types.ByteSize;

import java.util.function.Consumer;

import static naitsirc98.beryl.util.types.DataType.*;

@ByteSize.Static(PhongMaterial.SIZEOF)
public class PhongMaterial extends ManagedMaterial {

    public static final int SIZEOF = 4 * VECTOR4_SIZEOF + 6 * SAMPLER_SIZEOF + VECTOR2_SIZEOF
            + 4 * FLOAT32_SIZEOF + INT32_SIZEOF + FLOAT32_SIZEOF;

    // FLAGS

    private static final int NORMAL_MAP_PRESENT = 0x1;

    // === //

    private static final float DEFAULT_ALPHA = 1.0f;
    private static final float DEFAULT_SHININESS = 1.0f;
    private static final float DEFAULT_REFLECTIVITY = 0.0f;
    private static final float DEFAULT_REFRACTIVE_INDEX = 0.0f;

    private static final MaterialFactory<PhongMaterial> FACTORY = new MaterialFactory<>(PhongMaterial.class);

    public static MaterialFactory<PhongMaterial> getFactory() {
        return FACTORY;
    }


    private Color ambientColor;
    private Color diffuseColor;
    private Color specularColor;
    private Color emissiveColor;

    private Texture2D ambientMap;
    private Texture2D diffuseMap;
    private Texture2D specularMap;
    private Texture2D emissiveMap;
    private Texture2D occlusionMap;
    private Texture2D normalMap;

    private float alpha;
    private float shininess;
    private float reflectivity;
    private float refractiveIndex;

    PhongMaterial(String name) {
        super(name);
        setupDefaults();
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    public IColor getAmbientColor() {
        return ambientColor;
    }

    public PhongMaterial setAmbientColor(IColor ambientColor) {
        this.ambientColor.set(ambientColor);
        markModified();
        return this;
    }

    public IColor getDiffuseColor() {
        return diffuseColor;
    }

    public PhongMaterial setDiffuseColor(IColor diffuseColor) {
        this.diffuseColor.set(diffuseColor);
        markModified();
        return this;
    }

    public IColor getSpecularColor() {
        return specularColor;
    }

    public PhongMaterial setSpecularColor(IColor specularColor) {
        this.specularColor.set(specularColor);
        markModified();
        return this;
    }

    public IColor getEmissiveColor() {
        return emissiveColor;
    }

    public PhongMaterial setEmissiveColor(IColor emissiveColor) {
        this.emissiveColor.set(emissiveColor);
        markModified();
        return this;
    }

    public Texture2D getAmbientMap() {
        return getMapOrDefault(ambientMap);
    }

    public PhongMaterial setAmbientMap(Texture2D ambientMap) {
        updateTexturesUseCount(this.ambientMap, ambientMap);
        this.ambientMap = ambientMap;
        markModified();
        return this;
    }

    public Texture2D getDiffuseMap() {
        return getMapOrDefault(diffuseMap);
    }

    public PhongMaterial setDiffuseMap(Texture2D diffuseMap) {
        updateTexturesUseCount(this.diffuseMap, diffuseMap);
        this.diffuseMap = diffuseMap;
        markModified();
        return this;
    }

    public PhongMaterial setColorMap(Texture2D colorMap) {
        return setAmbientMap(colorMap).setDiffuseMap(colorMap);
    }

    public Texture2D getSpecularMap() {
        return getMapOrDefault(specularMap);
    }

    public PhongMaterial setSpecularMap(Texture2D specularMap) {
        updateTexturesUseCount(this.specularMap, specularMap);
        this.specularMap = specularMap;
        markModified();
        return this;
    }

    public Texture2D getEmissiveMap() {
        return getMapOrDefault(emissiveMap);
    }

    public PhongMaterial setEmissiveMap(Texture2D emissiveMap) {
        updateTexturesUseCount(this.emissiveMap, emissiveMap);
        this.emissiveMap = emissiveMap;
        markModified();
        return this;
    }

    public Texture2D getOcclusionMap() {
        return getMapOrDefault(occlusionMap);
    }

    public PhongMaterial setOcclusionMap(Texture2D occlusionMap) {
        updateTexturesUseCount(this.occlusionMap, occlusionMap);
        this.occlusionMap = occlusionMap;
        markModified();
        return this;
    }

    public Texture2D getNormalMap() {
        return getMapOrDefault(normalMap);
    }

    public PhongMaterial setNormalMap(Texture2D normalMap) {

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

    public PhongMaterial setAlpha(float alpha) {
        this.alpha = alpha;
        markModified();
        return this;
    }

    public float getShininess() {
        return shininess;
    }

    public PhongMaterial setShininess(float shininess) {
        this.shininess = shininess;
        markModified();
        return this;
    }

    public float getReflectivity() {
        return reflectivity;
    }

    public PhongMaterial setReflectivity(float reflectivity) {
        this.reflectivity = reflectivity;
        markModified();
        return this;
    }

    public float getRefractiveIndex() {
        return refractiveIndex;
    }

    public PhongMaterial setRefractiveIndex(float refractiveIndex) {
        this.refractiveIndex = refractiveIndex;
        markModified();
        return this;
    }

    private void setupDefaults() {

        ambientColor = Color.colorWhite();
        diffuseColor = Color.colorWhite();
        specularColor = Color.colorWhite();
        emissiveColor = Color.colorBlackTransparent();

        alpha = DEFAULT_ALPHA;
        shininess = DEFAULT_SHININESS;
        reflectivity = DEFAULT_REFLECTIVITY;
        refractiveIndex = DEFAULT_REFRACTIVE_INDEX;
    }

    @Override
    public ShadingModel shadingModel() {
        return ShadingModel.PHONG;
    }
}
