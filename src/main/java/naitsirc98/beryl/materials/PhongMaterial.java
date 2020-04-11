package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.util.BitFlags;
import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.types.ByteSize;
import naitsirc98.beryl.util.types.IBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;

@ByteSize.Static(PhongMaterial.SIZEOF)
public interface PhongMaterial extends IMaterial {

    int SIZEOF = Color.SIZEOF * 4 + 4 * FLOAT32_SIZEOF + 6 * Texture.SIZEOF;

    String PHONG_MATERIAL_DEFAULT_NAME = "DEFAULT_PHONG_MATERIAL";

    static PhongMaterial getDefault() {
        return MaterialManager.get().get(PHONG_MATERIAL_DEFAULT_NAME);
    }

    static PhongMaterial get(String name, Consumer<Builder> builderConsumer) {
        if(MaterialManager.get().exists(name)) {
            return MaterialManager.get().get(name);
        }
        Builder builder = new Builder().name(name);
        builderConsumer.accept(builder);
        return builder.build();
    }

    static Builder builder() {
        return new Builder();
    }

    Color ambientColor();
    Color diffuseColor();
    Color specularColor();
    Color emissiveColor();

    Texture2D ambientMap();
    Texture2D diffuseMap();
    Texture2D specularMap();
    Texture2D emissiveMap();
    Texture2D occlusionMap();
    Texture2D normalMap();

    float alpha();
    float shininess();
    float reflectivity();
    float refractiveIndex();


    final class Builder {

        private final Map<Byte, Object> properties;
        private final BitFlags flags;
        private String name;

        public Builder() {
            properties = new HashMap<>();
            flags = new BitFlags();
            setDefaults();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder ambientColor(Color color) {
            properties.put(AMBIENT_COLOR, color);
            return this;
        }

        public Builder diffuseColor(Color color) {
            properties.put(DIFFUSE_COLOR, color);
            return this;
        }

        public Builder specularColor(Color color) {
            properties.put(SPECULAR_COLOR, color);
            return this;
        }

        public Builder emissiveColor(Color color) {
            properties.put(EMISSIVE_COLOR, color);
            return this;
        }

        public Builder ambientMap(Texture2D map) {
            properties.put(AMBIENT_MAP, map);
            return this;
        }

        public Builder diffuseMap(Texture2D map) {
            properties.put(DIFFUSE_MAP, map);
            return this;
        }

        public Builder specularMap(Texture2D map) {
            properties.put(SPECULAR_MAP, map);
            return this;
        }

        public Builder emissiveMap(Texture2D map) {
            properties.put(EMISSIVE_MAP, map);
            return this;
        }

        public Builder occlusionMap(Texture2D map) {
            properties.put(OCCLUSION_MAP, map);
            return this;
        }

        public Builder normalMap(Texture2D map) {
            properties.put(NORMAL_MAP, map);
            return this;
        }

        public Builder alpha(float value) {
            properties.put(ALPHA, value);
            return this;
        }

        public Builder shininess(float value) {
            properties.put(SHININESS, value);
            return this;
        }

        public Builder reflectivity(float value) {
            properties.put(REFLECTIVITY, value);
            return this;
        }

        public Builder refractiveIndex(float value) {
            properties.put(REFRACTIVE_INDEX, value);
            return this;
        }

        private PhongMaterial build() {
            return MaterialManager.get().create(name, flags, properties);
        }

        private void setDefaults() {

            flags.enable(PHONG_MATERIAL_BIT);

            properties.put(AMBIENT_COLOR, Color.WHITE);
            properties.put(DIFFUSE_COLOR, Color.WHITE);
            properties.put(SPECULAR_COLOR, Color.BLACK);
            properties.put(EMISSIVE_COLOR, Color.BLACK);

            properties.put(ALPHA, 1.0f);
            properties.put(SHININESS, 0.0f);
            properties.put(REFLECTIVITY, 0.0f);
            properties.put(REFRACTIVE_INDEX, 0.0f);

            Texture2D blankTexture = GraphicsFactory.get().blankTexture2D();

            properties.put(AMBIENT_MAP, blankTexture);
            properties.put(DIFFUSE_MAP, blankTexture);
            properties.put(SPECULAR_MAP, blankTexture);
            properties.put(EMISSIVE_MAP, blankTexture);
            properties.put(NORMAL_MAP, blankTexture);
            properties.put(OCCLUSION_MAP, blankTexture);
        }
    }
}
