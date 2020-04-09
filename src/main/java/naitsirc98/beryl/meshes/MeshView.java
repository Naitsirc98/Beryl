package naitsirc98.beryl.meshes;

import naitsirc98.beryl.materials.Material;

import java.util.Objects;

public final class MeshView {

    private final Mesh mesh;
    private final Material material;

    public MeshView(Mesh mesh, Material material) {
        this.mesh = mesh;
        this.material = material;
    }

    public Mesh mesh() {
        return mesh;
    }

    public Material material() {
        return material;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeshView meshView = (MeshView) o;
        return Objects.equals(mesh, meshView.mesh) &&
                Objects.equals(material, meshView.material);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mesh, material);
    }
}
