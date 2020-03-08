package naitsirc98.beryl.graphics.opengl.materials;

import naitsirc98.beryl.images.Image;
import naitsirc98.beryl.meshes.materials.BlinnPhongMaterial;
import naitsirc98.beryl.util.Color;

public class GLBlinnPhongMaterial extends BlinnPhongMaterial {

    private static final float DEFAULT_SHININESS = 32.0f;


    private GLTextureColorMaterialProperty ambient;
    private GLTextureColorMaterialProperty diffuse;
    private GLTextureColorMaterialProperty specular;
    private float shininess;

    public GLBlinnPhongMaterial() {
        ambient = new GLTextureColorMaterialProperty();
        diffuse = new GLTextureColorMaterialProperty();
        specular = new GLTextureColorMaterialProperty();
        shininess = DEFAULT_SHININESS;
    }

    @Override
    public BlinnPhongMaterial color(Color color) {
        ambient.color(color);
        return this;
    }

    @Override
    public BlinnPhongMaterial map(Image map) {
        return this;
    }

    @Override
    public Color ambientColor() {
        return ambient.color();
    }

    @Override
    public BlinnPhongMaterial ambientColor(Color ambientColor) {
        return this;
    }

    @Override
    public BlinnPhongMaterial ambientMap(Image ambientMap) {
        return this;
    }

    @Override
    public Color diffuseColor() {
        return diffuse.color();
    }

    @Override
    public BlinnPhongMaterial diffuseColor(Color ambientColor) {
        return this;
    }

    @Override
    public BlinnPhongMaterial diffuseMap(Image ambientMap) {
        return this;
    }

    @Override
    public Color specularColor() {
        return specular.color();
    }

    @Override
    public BlinnPhongMaterial specularColor(Color ambientColor) {
        return this;
    }

    @Override
    public BlinnPhongMaterial specularMap(Image ambientMap) {
        return this;
    }

    @Override
    public float shininess() {
        return shininess;
    }

    @Override
    public BlinnPhongMaterial shininess(float shininess) {
        this.shininess = shininess;
        return this;
    }

    @Override
    public void free() {

    }
}
