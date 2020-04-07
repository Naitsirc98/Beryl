package naitsirc98.beryl.materials;

import naitsirc98.beryl.util.types.ByteSize;

import java.nio.ByteBuffer;

import static java.util.Objects.requireNonNull;

public abstract class Material implements ByteSize {

    private final String name;
    private final boolean transparent;
    private final int hashCode;

    public Material(String name, boolean transparent) {
        this.name = requireNonNull(name);
        this.transparent = transparent;
        this.hashCode = Materials.nextHashCode();
        Materials.register(this);
    }

    public final String name() {
        return name;
    }

    public final boolean transparent() {
        return transparent;
    }

    public abstract ByteBuffer get(int offset, ByteBuffer buffer);

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Material material = (Material) o;
        return hashCode == material.hashCode;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "name='" + name + '\'' +
                '}';
    }
}
