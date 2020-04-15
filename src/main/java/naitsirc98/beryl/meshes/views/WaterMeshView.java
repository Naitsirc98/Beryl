package naitsirc98.beryl.meshes.views;

import naitsirc98.beryl.materials.WaterMaterial;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.util.Color;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import static java.util.Objects.requireNonNull;

public final class WaterMeshView extends StaticMeshView {

    private final Vector4f clipPlane;
    private float distortionStrength;
    private float texturesOffset;
    private Color waterColor;
    private float waterColorStrength;
    private float tiling;

    public WaterMeshView(StaticMesh mesh, WaterMaterial material) {
        super(mesh, material);
        this.clipPlane = new Vector4f(0, 1, 0, 0);
        distortionStrength = 0.02f;
        texturesOffset = 0.0f;
        waterColor = Color.WATER;
        waterColorStrength = 0.2f;
        tiling = 1;
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

    public float distortionStrength() {
        return distortionStrength;
    }

    public WaterMeshView distortionStrength(float distortionStrength) {
        this.distortionStrength = distortionStrength;
        return this;
    }

    public float texturesOffset() {
        return texturesOffset;
    }

    public WaterMeshView texturesOffset(float texturesOffset) {
        this.texturesOffset = texturesOffset;
        return this;
    }

    public Color waterColor() {
        return waterColor;
    }

    public WaterMeshView waterColor(Color waterColor) {
        this.waterColor = requireNonNull(waterColor);
        return this;
    }

    public float waterColorStrength() {
        return waterColorStrength;
    }

    public WaterMeshView waterColorStrength(float waterColorStrength) {
        this.waterColorStrength = waterColorStrength;
        return this;
    }

    public float tiling() {
        return tiling;
    }

    public WaterMeshView tiling(float tiling) {
        this.tiling = tiling;
        return this;
    }
}
