package naitsirc98.beryl.meshes.views;

import naitsirc98.beryl.materials.IMaterial;
import naitsirc98.beryl.meshes.Mesh;

import java.util.Objects;

public abstract class MeshView<T extends Mesh> {

    private final T mesh;
    private final IMaterial material;

    public MeshView(T mesh, IMaterial material) {
        this.mesh = mesh;
        this.material = material;
    }

    @SuppressWarnings("unchecked")
    public <U extends T> U mesh() {
        return (U) mesh;
    }

    public IMaterial material() {
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

    @Override
    public String toString() {
        return "MeshView{" +
                "mesh=" + mesh.name() +
                ", material=" + material.name() +
                '}';
    }
}
