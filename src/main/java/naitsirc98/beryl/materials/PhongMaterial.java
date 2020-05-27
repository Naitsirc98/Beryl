package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.rendering.ShadingModel;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.IColor;
import naitsirc98.beryl.util.types.ByteSize;

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

    public IColor ambientColor() {
        return ambientColor;
    }

    public PhongMaterial ambientColor(IColor ambientColor) {
        this.ambientColor.set(ambientColor);
        markModified();
        return this;
    }

    public IColor diffuseColor() {
        return diffuseColor;
    }

    public PhongMaterial diffuseColor(IColor diffuseColor) {
        this.diffuseColor.set(diffuseColor);
        markModified();
        return this;
    }

    public IColor specularColor() {
        return specularColor;
    }

    public PhongMaterial specularColor(IColor specularColor) {
        this.specularColor.set(specularColor);
        markModified();
        return this;
    }

    public IColor emissiveColor() {
        return emissiveColor;
    }

    public PhongMaterial emissiveColor(IColor emissiveColor) {
        this.emissiveColor.set(emissiveColor);
        markModified();
        return this;
    }

    public Texture2D ambientMap() {
        return getMapOrDefault(ambientMap);
    }

    public PhongMaterial ambientMap(Texture2D ambientMap) {
        updateTexturesUseCount(this.ambientMap, ambientMap);
        this.ambientMap = ambientMap;
        markModified();
        return this;
    }

    public Texture2D diffuseMap() {
        return getMapOrDefault(diffuseMap);
    }

    public PhongMaterial diffuseMap(Texture2D diffuseMap) {
        updateTexturesUseCount(this.diffuseMap, diffuseMap);
        this.diffuseMap = diffuseMap;
        markModified();
        return this;
    }

    public PhongMaterial colorMap(Texture2D colorMap) {
        return ambientMap(colorMap).diffuseMap(colorMap);
    }

    public Texture2D specularMap() {
        return getMapOrDefault(specularMap);
    }

    public PhongMaterial specularMap(Texture2D specularMap) {
        updateTexturesUseCount(this.specularMap, specularMap);
        this.specularMap = specularMap;
        markModified();
        return this;
    }

    public Texture2D emissiveMap() {
        return getMapOrDefault(emissiveMap);
    }

    public PhongMaterial emissiveMap(Texture2D emissiveMap) {
        updateTexturesUseCount(this.emissiveMap, emissiveMap);
        this.emissiveMap = emissiveMap;
        markModified();
        return this;
    }

    public Texture2D occlusionMap() {
        return getMapOrDefault(occlusionMap);
    }

    public PhongMaterial occlusionMap(Texture2D occlusionMap) {
        updateTexturesUseCount(this.occlusionMap, occlusionMap);
        this.occlusionMap = occlusionMap;
        markModified();
        return this;
    }

    public Texture2D normalMap() {
        return getMapOrDefault(normalMap);
    }

    public PhongMaterial normalMap(Texture2D normalMap) {

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

    public float alpha() {
        return alpha;
    }

    public PhongMaterial alpha(float alpha) {
        this.alpha = alpha;
        markModified();
        return this;
    }

    public float shininess() {
        return shininess;
    }

    public PhongMaterial shininess(float shininess) {
        this.shininess = shininess;
        markModified();
        return this;
    }

    public float reflectivity() {
        return reflectivity;
    }

    public PhongMaterial reflectivity(float reflectivity) {
        this.reflectivity = reflectivity;
        markModified();
        return this;
    }

    public float refractiveIndex() {
        return refractiveIndex;
    }

    public PhongMaterial refractiveIndex(float refractiveIndex) {
        this.refractiveIndex = refractiveIndex;
        markModified();
        return this;
    }

    @Override
    public ShadingModel shadingModel() {
        return ShadingModel.PHONG;
    }

    private void setupDefaults() {

        ambientColor = Color.colorWhite();
        diffuseColor = Color.colorWhite();
        specularColor = Color.colorBlackTransparent();
        emissiveColor = Color.colorBlackTransparent();

        alpha = DEFAULT_ALPHA;
        shininess = DEFAULT_SHININESS;
        reflectivity = DEFAULT_REFLECTIVITY;
        refractiveIndex = DEFAULT_REFRACTIVE_INDEX;
    }
}
