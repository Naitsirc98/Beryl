package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.types.ByteSize;
import naitsirc98.beryl.util.types.IBuilder;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;

@ByteSize.Static(PhongMaterial.SIZEOF)
public class PhongMaterial extends Material implements ByteSize {

    public static final int SIZEOF = 5 * 4 * FLOAT32_SIZEOF; // Does not count the textures
    public static final int FLOAT_BUFFER_MIN_SIZE = SIZEOF / FLOAT32_SIZEOF;

    public static final float DEFAULT_SHININESS = 32.0f;

    private final ColorMapProperty ambient;
    private final ColorMapProperty diffuse;
    private final ColorMapProperty specular;
    private final ColorMapProperty emissive;
    private final float shininess;

    private PhongMaterial(String name, ColorMapProperty ambient, ColorMapProperty diffuse,
                         ColorMapProperty specular, ColorMapProperty emissive,
                         float shininess) {

        super(name);
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.emissive = emissive;
        this.shininess = shininess;
    }

    // This modifies the buffer's position!
    public ByteBuffer get(ByteBuffer buffer) {
        assertTrue(buffer.remaining() >= SIZEOF);

        ambientColor().getRGBA(buffer);
        diffuseColor().getRGBA(buffer);
        specularColor().getRGBA(buffer);
        emissiveColor().getRGBA(buffer);
        buffer.putFloat(shininess());

        return buffer;
    }

    // This modifies the buffer's position!
    public FloatBuffer get(FloatBuffer buffer) {
        assertTrue(buffer.remaining() >= FLOAT_BUFFER_MIN_SIZE);

        ambientColor().getRGBA(buffer);
        diffuseColor().getRGBA(buffer);
        specularColor().getRGBA(buffer);
        emissiveColor().getRGBA(buffer);
        buffer.put(shininess());

        return buffer;
    }

    public Color ambientColor() {
        return ambient.color();
    }

    public <T extends Texture2D> T ambientMap() {
        return ambient.map();
    }

    public Color diffuseColor() {
        return diffuse.color();
    }

    public <T extends Texture2D> T diffuseMap() {
        return diffuse.map();
    }

    public Color specularColor() {
        return specular.color();
    }

    public <T extends Texture2D> T specularMap() {
        return specular.map();
    }

    public Color emissiveColor() {
        return emissive.color();
    }

    public <T extends Texture2D> T emissiveMap() {
        return emissive.map();
    }

    public float shininess() {
        return shininess;
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    public static final class Builder implements IBuilder<PhongMaterial> {

        private final String name;
        private final ColorMapProperty ambient;
        private final ColorMapProperty diffuse;
        private final ColorMapProperty specular;
        private final ColorMapProperty emissive;
        private float shininess;

        public Builder(String name) {
            this.name = requireNonNull(name);
            ambient = new ColorMapProperty();
            diffuse = new ColorMapProperty();
            specular = new ColorMapProperty();
            emissive = new ColorMapProperty();
            emissive.color(Color.BLACK);
            shininess = DEFAULT_SHININESS;
        }

        public Builder color(Color color) {
            return ambientColor(color).diffuseColor(color).specularColor(color);
        }

        public Builder map(Texture2D map) {
            return ambientMap(map).diffuseMap(map).specularMap(map);
        }

        public Builder ambientColor(Color ambientColor) {
            ambient.color(ambientColor);
            return this;
        }

        public Builder ambientMap(Texture2D ambientMap) {
            ambient.map(ambientMap);
            return this;
        }

        public Builder diffuseColor(Color diffuseColor) {
            diffuse.color(diffuseColor);
            return this;
        }

        public Builder diffuseMap(Texture2D diffuseMap) {
            diffuse.map(diffuseMap);
            return this;
        }

        public Builder specularColor(Color specularColor) {
            specular.color(specularColor);
            return this;
        }

        public Builder specularMap(Texture2D specularMap) {
            specular.map(specularMap);
            return this;
        }

        public Builder emissiveColor(Color emissiveColor) {
            emissive.color(emissiveColor);
            return this;
        }

        public Builder emissiveMap(Texture2D emissiveMap) {
            emissive.map(emissiveMap);
            return this;
        }

        public Builder shininess(float shininess) {
            this.shininess = shininess;
            return this;
        }

        @Override
        public PhongMaterial build() {
            return new PhongMaterial(name, ambient, diffuse, specular, emissive, shininess);
        }
    }
}
