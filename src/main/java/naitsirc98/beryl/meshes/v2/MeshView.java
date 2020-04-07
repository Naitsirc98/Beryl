package naitsirc98.beryl.meshes.v2;

import naitsirc98.beryl.materials.Material;

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
}
