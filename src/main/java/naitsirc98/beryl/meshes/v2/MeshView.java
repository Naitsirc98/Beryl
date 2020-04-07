package naitsirc98.beryl.meshes.v2;

import naitsirc98.beryl.materials.Material;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public class MeshView {

    private final Mesh mesh;
    private Material material;
    private Matrix4f modelMatrix;

    public MeshView(Mesh mesh, Material material) {
        this.mesh = mesh;
        this.material = material;
        modelMatrix = new Matrix4f(); // TODO remove this, use entity transform
    }

    public Mesh mesh() {
        return mesh;
    }

    public Material material() {
        return material;
    }

    public MeshView material(Material material) {
        this.material = material;
        return this;
    }

    public Matrix4fc modelMatrix() {
        return modelMatrix;
    }

    public MeshView modelMatrix(Matrix4fc modelMatrix) {
        this.modelMatrix.set(modelMatrix);
        return this;
    }
}
