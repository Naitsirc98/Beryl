package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.types.ByteSize;

import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;

@ByteSize.Static(PhongMaterial.SIZEOF)
public class PhongMaterial implements Material, ByteSize {

    public static final int SIZEOF = 4 * ColorMapProperty.SIZEOF + FLOAT32_SIZEOF;

    public static final float DEFAULT_SHININESS = 32.0f;

    private final ColorMapProperty ambient;
    private final ColorMapProperty diffuse;
    private final ColorMapProperty specular;
    private final ColorMapProperty emissive;
    private float shininess;

    public PhongMaterial() {
        ambient = new ColorMapProperty();
        diffuse = new ColorMapProperty();
        specular = new ColorMapProperty();
        emissive = new ColorMapProperty();
        shininess = DEFAULT_SHININESS;
    }

    public PhongMaterial color(Color color) {
        return ambientColor(color).diffuseColor(color).specularColor(color);
    }

    public PhongMaterial map(Texture2D map) {
        return ambientMap(map).diffuseMap(map).specularMap(map);
    }

    public Color ambientColor() {
        return ambient.color();
    }

    public <T extends Texture2D> T ambientMap() {
        return ambient.map();
    }

    public PhongMaterial ambientColor(Color ambientColor) {
        ambient.color(ambientColor);
        return this;
    }

    public PhongMaterial ambientMap(Texture2D ambientMap) {
        ambient.map(ambientMap);
        return this;
    }

    public Color diffuseColor() {
        return diffuse.color();
    }

    public <T extends Texture2D> T diffuseMap() {
        return diffuse.map();
    }

    public PhongMaterial diffuseColor(Color diffuseColor) {
        diffuse.color(diffuseColor);
        return this;
    }

    public PhongMaterial diffuseMap(Texture2D diffuseMap) {
        diffuse.map(diffuseMap);
        return this;
    }

    public Color specularColor() {
        return specular.color();
    }

    public <T extends Texture2D> T specularMap() {
        return specular.map();
    }

    public PhongMaterial specularColor(Color specularColor) {
        specular.color(specularColor);
        return this;
    }

    public PhongMaterial specularMap(Texture2D specularMap) {
        specular.map(specularMap);
        return this;
    }

    public Color emissiveColor() {
        return emissive.color();
    }

    public <T extends Texture2D> T emissiveMap() {
        return emissive.map();
    }

    public PhongMaterial emissiveColor(Color emissiveColor) {
        emissive.color(emissiveColor);
        return this;
    }

    public PhongMaterial emissiveMap(Texture2D emissiveMap) {
        emissive.map(emissiveMap);
        return this;
    }

    public float shininess() {
        return shininess;
    }

    public PhongMaterial shininess(float shininess) {
        this.shininess = shininess;
        return this;
    }

    @Override
    public final ShadingModel shadingModel() {
        return ShadingModel.BLINN_PHONG;
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }
}
