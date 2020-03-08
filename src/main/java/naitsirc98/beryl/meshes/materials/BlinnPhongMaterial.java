package naitsirc98.beryl.meshes.materials;

import naitsirc98.beryl.images.Image;
import naitsirc98.beryl.util.Color;

public abstract class BlinnPhongMaterial implements Material {

    public abstract BlinnPhongMaterial color(Color color);
    public abstract BlinnPhongMaterial map(Image map);

    public abstract Color ambientColor();
    public abstract BlinnPhongMaterial ambientColor(Color ambientColor);
    public abstract BlinnPhongMaterial ambientMap(Image ambientMap);

    public abstract Color diffuseColor();
    public abstract BlinnPhongMaterial diffuseColor(Color ambientColor);
    public abstract BlinnPhongMaterial diffuseMap(Image ambientMap);

    public abstract Color specularColor();
    public abstract BlinnPhongMaterial specularColor(Color ambientColor);
    public abstract BlinnPhongMaterial specularMap(Image ambientMap);

    public abstract float shininess();
    public abstract BlinnPhongMaterial shininess(float shininess);

    @Override
    public final ShadingModel shadingModel() {
        return ShadingModel.BLINN_PHONG;
    }
}
