package naitsirc98.beryl.meshes.views;

import naitsirc98.beryl.materials.WaterMaterial;
import naitsirc98.beryl.meshes.StaticMesh;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public final class WaterMeshView extends StaticMeshView {

    private final Vector4f clipPlane;

    public WaterMeshView(StaticMesh mesh, WaterMaterial material) {
        super(mesh, material);
        this.clipPlane = new Vector4f(0, 1, 0, 0);
    }

    @Override
    public WaterMaterial material() {
        return (WaterMaterial) super.material();
    }

    public Vector4f clipPlane() {
        return clipPlane;
    }

    public WaterMeshView clipPlane(Vector4fc clipPlane) {
        this.clipPlane.set(clipPlane);
        return this;
    }

    public WaterMeshView clipPlane(Vector3fc planeNormal, float height) {
        this.clipPlane.set(planeNormal, height);
        return this;
    }

    public WaterMeshView clipPlane(float x, float y, float z, float w) {
        clipPlane.set(x, y, z, w);
        return this;
    }
}
