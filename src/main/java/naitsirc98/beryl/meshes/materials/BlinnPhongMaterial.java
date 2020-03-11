package naitsirc98.beryl.meshes.materials;

import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.util.Color;

public class BlinnPhongMaterial implements Material {

    public static final float DEFAULT_SHININESS = 32.0f;

    private final ColorMapProperty ambient;
    private final ColorMapProperty diffuse;
    private final ColorMapProperty specular;
    private final ColorMapProperty emissive;
    private float shininess;

    public BlinnPhongMaterial() {
        ambient = new ColorMapProperty();
        diffuse = new ColorMapProperty();
        specular = new ColorMapProperty();
        emissive = new ColorMapProperty();
        shininess = DEFAULT_SHININESS;
    }

    public BlinnPhongMaterial color(Color color) {
        return ambientColor(color).diffuseColor(color).specularColor(color);
    }

    public BlinnPhongMaterial map(Texture2D map) {
        return ambientMap(map).diffuseMap(map).specularMap(map);
    }

    public Color ambientColor() {
        return ambient.color();
    }

    public Texture2D ambientMap() {
        return ambient.map();
    }

    public BlinnPhongMaterial ambientColor(Color ambientColor) {
        ambient.color(ambientColor);
        return this;
    }

    public BlinnPhongMaterial ambientMap(Texture2D ambientMap) {
        ambient.map(ambientMap);
        return this;
    }

    public Color diffuseColor() {
        return diffuse.color();
    }

    public Texture2D diffuseMap() {
        return diffuse.map();
    }

    public BlinnPhongMaterial diffuseColor(Color diffuseColor) {
        diffuse.color(diffuseColor);
        return this;
    }

    public BlinnPhongMaterial diffuseMap(Texture2D diffuseMap) {
        diffuse.map(diffuseMap);
        return this;
    }

    public Color specularColor() {
        return specular.color();
    }

    public Texture2D specularMap() {
        return specular.map();
    }

    public BlinnPhongMaterial specularColor(Color specularColor) {
        specular.color(specularColor);
        return this;
    }

    public BlinnPhongMaterial specularMap(Texture2D specularMap) {
        specular.map(specularMap);
        return this;
    }

    public Color emissiveColor() {
        return emissive.color();
    }

    public Texture2D emissiveMap() {
        return emissive.map();
    }

    public BlinnPhongMaterial emissiveColor(Color emissiveColor) {
        emissive.color(emissiveColor);
        return this;
    }

    public BlinnPhongMaterial emissiveMap(Texture2D emissiveMap) {
        emissive.map(emissiveMap);
        return this;
    }

    public float shininess() {
        return shininess;
    }

    public BlinnPhongMaterial shininess(float shininess) {
        this.shininess = shininess;
        return this;
    }

    @Override
    public final ShadingModel shadingModel() {
        return ShadingModel.BLINN_PHONG;
    }

}
