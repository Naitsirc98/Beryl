package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.util.BitFlags;
import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.types.ByteSize;
import org.joml.Vector2fc;

import java.util.Collections;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@ByteSize.Static(IMaterial.SIZEOF)
public class Material implements IMaterial, PhongMaterial, WaterMaterial {

    private final transient int handle;
    private final String name;
    private final Type type;
    private final Map<Byte, Object> properties;
    private final BitFlags flags;
    private transient long offset = -Long.MAX_VALUE; // Offset of this material into the materials buffer
    private transient int index = Integer.MIN_VALUE; // This is the index of this material in this material's type list
    private transient boolean destroyed;
    private transient boolean modified;

    Material(int handle, String name, Type type, Map<Byte, Object> properties, BitFlags flags) {
        this.handle = handle;
        this.name = name;
        this.type = type;
        this.properties = properties;
        this.flags = flags;
    }

    @Override
    public Vector2fc textureCoordsFactor() {
        return get(TEXTURE_COORDS_FACTOR);
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public long offset() {
        return offset;
    }

    @Override
    public int bufferIndex() {
        return (int) (offset / SIZEOF);
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public int flags() {
        return flags.get();
    }

    @Override
    public boolean modified() {
        return modified;
    }

    void markUpdated() {
        modified = false;
    }

    @Override
    public boolean destroyed() {
        return destroyed;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int handle() {
        return handle;
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @Override
    public Color ambientColor() {
        return get(AMBIENT_COLOR);
    }

    @Override
    public PhongMaterial ambientColor(Color color) {
        properties.put(AMBIENT_COLOR, requireNonNull(color));
        modify();
        return this;
    }

    @Override
    public Color diffuseColor() {
        return get(DIFFUSE_COLOR);
    }

    @Override
    public PhongMaterial diffuseColor(Color color) {
        properties.put(DIFFUSE_COLOR, requireNonNull(color));
        modify();
        return this;
    }

    @Override
    public Color specularColor() {
        return get(SPECULAR_COLOR);
    }

    @Override
    public PhongMaterial specularColor(Color color) {
        properties.put(SPECULAR_COLOR, requireNonNull(color));
        modify();
        return this;
    }

    @Override
    public Color emissiveColor() {
        return get(EMISSIVE_COLOR);
    }

    @Override
    public PhongMaterial emissiveColor(Color color) {
        properties.put(EMISSIVE_COLOR, requireNonNull(color));
        modify();
        return this;
    }

    @Override
    public float alpha() {
        return get(ALPHA);
    }

    @Override
    public float shininess() {
        return get(SHININESS);
    }

    @Override
    public float reflectivity() {
        return get(REFLECTIVITY);
    }

    @Override
    public float refractiveIndex() {
        return get(REFRACTIVE_INDEX);
    }

    @Override
    public Texture2D ambientMap() {
        return get(AMBIENT_MAP);
    }

    @Override
    public Texture2D diffuseMap() {
        return get(DIFFUSE_MAP);
    }

    @Override
    public Texture2D specularMap() {
        return get(SPECULAR_MAP);
    }

    @Override
    public Texture2D emissiveMap() {
        return get(EMISSIVE_MAP);
    }

    @Override
    public Texture2D occlusionMap() {
        return get(OCCLUSION_MAP);
    }

    @Override
    public Texture2D normalMap() {
        return get(NORMAL_MAP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Material material = (Material) o;
        return handle == material.handle;
    }

    @Override
    public int hashCode() {
        return handle;
    }

    void setOffset(long address) {
        this.offset = address;
    }

    void setIndex(int index) {
        this.index = index;
    }

    void destroy() {
        destroyed = true;
    }

    @SuppressWarnings("unchecked")
    private <T> T get(int propertyID) {
        return (T) properties.get((byte)propertyID);
    }

    @Override
    public Texture2D reflectionMap() {
        return get(REFLECTION_MAP);
    }

    @Override
    public Texture2D refractionMap() {
        return get(REFRACTION_MAP);
    }

    @Override
    public Texture2D dudvMap() {
        return get(DUDV_MAP);
    }

    private void modify() {
        if(!modified) {
            modified = true;
            MaterialManager.get().setModified(this);
        }
    }
}
