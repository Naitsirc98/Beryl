package naitsirc98.beryl.meshes;

import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.meshes.vertices.VertexData;

import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;

public class Mesh {

    private static final AtomicInteger HASH = new AtomicInteger(0);

    private final VertexData vertexData;
    private final Material material;
    private final int hashCode;

    public Mesh(VertexData vertexData, Material material) {
        this.vertexData = requireNonNull(vertexData);
        this.material = requireNonNull(material);
        hashCode = HASH.getAndIncrement();
    }

    @SuppressWarnings("unchecked")
    public final <T extends VertexData> T vertexData() {
        return (T) vertexData;
    }

    @SuppressWarnings("unchecked")
    public final <T extends Material> T material() {
        return (T) material;
    }

    public boolean instanced() {
        return vertexData.instanced();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mesh mesh = (Mesh) o;
        return hashCode == mesh.hashCode;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
