package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.util.BitFlags;
import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.types.ByteSize;
import org.joml.Vector2f;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static naitsirc98.beryl.materials.IMaterial.Type.PHONG_MATERIAL;

@ByteSize.Static(IMaterial.SIZEOF)
public interface PhongMaterial extends IMaterial {

    String PHONG_MATERIAL_DEFAULT_NAME = "DEFAULT_PHONG_MATERIAL";

    static PhongMaterial getDefault() {
        return MaterialManager.get().get(PHONG_MATERIAL_DEFAULT_NAME);
    }

    static boolean exists(String name) {
        return MaterialManager.get().exists(name);
    }

    static PhongMaterial get(String name) {
        if(MaterialManager.get().exists(name)) {
            return MaterialManager.get().get(name);
        }
        return MaterialManager.get().create(name, PHONG_MATERIAL, getDefaultProperties(), getDefaultFlags());
    }

    static PhongMaterial get(String name, Consumer<PhongMaterial> materialConfiguration) {
        if(exists(name)) {
            return get(name);
        }
        PhongMaterial material = get(name);
        materialConfiguration.accept(material);
        return material;
    }

    Color ambientColor();
    PhongMaterial ambientColor(Color color);

    Color diffuseColor();
    PhongMaterial diffuseColor(Color color);

    Color specularColor();
    PhongMaterial specularColor(Color color);

    Color emissiveColor();
    PhongMaterial emissiveColor(Color color);

    Texture2D ambientMap();
    PhongMaterial ambientMap(Texture2D ambientMap);

    Texture2D diffuseMap();
    PhongMaterial diffuseMap(Texture2D diffuseMap);

    Texture2D specularMap();
    PhongMaterial specularMap(Texture2D specularMap);

    Texture2D emissiveMap();
    PhongMaterial emissiveMap(Texture2D emissiveMap);

    Texture2D occlusionMap();
    PhongMaterial occlusionMap(Texture2D occlusionMap);

    Texture2D normalMap();
    PhongMaterial normalMap(Texture2D normalMap);

    float alpha();
    PhongMaterial alpha(float alpha);

    float shininess();
    PhongMaterial shininess(float shininess);

    float reflectivity();
    PhongMaterial reflectivity(float reflectivity);

    float refractiveIndex();
    PhongMaterial refractiveIndex(float refractiveIndex);



    private static BitFlags getDefaultFlags() {
        return new BitFlags();
    }

    private static Map<Byte, Object> getDefaultProperties() {

        Map<Byte, Object> properties = new HashMap<>();

        properties.put(AMBIENT_COLOR, Color.WHITE);
        properties.put(DIFFUSE_COLOR, Color.WHITE);
        properties.put(SPECULAR_COLOR, Color.BLACK);
        properties.put(EMISSIVE_COLOR, Color.BLACK);

        properties.put(ALPHA, 1.0f);
        properties.put(SHININESS, 1.0f);
        properties.put(REFLECTIVITY, 0.0f);
        properties.put(REFRACTIVE_INDEX, 0.0f);

        properties.put(TEXTURE_TILING, DEFAULT_TEXTURE_TILING);

        Texture2D blankTexture = GraphicsFactory.get().blankTexture2D();

        properties.put(AMBIENT_MAP, blankTexture);
        properties.put(DIFFUSE_MAP, blankTexture);
        properties.put(SPECULAR_MAP, blankTexture);

        return properties;
    }

}
