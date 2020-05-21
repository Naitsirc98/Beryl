package naitsirc98.beryl.meshes;

import naitsirc98.beryl.assets.Asset;
import naitsirc98.beryl.resources.ManagedResource;
import naitsirc98.beryl.util.geometry.AABB;
import naitsirc98.beryl.util.geometry.IAABB;
import naitsirc98.beryl.util.geometry.ISphere;
import naitsirc98.beryl.util.geometry.Sphere;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.UINT32_SIZEOF;
import static org.lwjgl.system.MemoryUtil.memFree;

public abstract class Mesh extends ManagedResource implements Asset {

    public static final int VERTEX_POSITION_OFFSET = 0;
    public static final int VERTEX_POSITION_SIZE = 3 * FLOAT32_SIZEOF;

    public static final int VERTEX_NORMAL_OFFSET = VERTEX_POSITION_SIZE;
    public static final int VERTEX_NORMAL_SIZE = 3 * FLOAT32_SIZEOF;

    public static final int VERTEX_TEXCOORDS_OFFSET = VERTEX_POSITION_SIZE + VERTEX_NORMAL_SIZE;
    public static final int VERTEX_TEXCOORDS_SIZE = 2 * FLOAT32_SIZEOF;


    protected final int handle;
    protected final String name;
    protected final ByteBuffer vertexData;
    protected final ByteBuffer indexData;
    protected final AABB boundingBox;
    protected final Sphere boundingSphere;
    private final MeshStorageInfo storageInfo;
    protected final int stride;

    public Mesh(int handle, String name, ByteBuffer vertexData, ByteBuffer indexData, int stride) {
        this.handle = handle;
        this.name = name;
        this.vertexData = requireNonNull(vertexData);
        this.indexData = requireNonNull(indexData);
        this.stride = stride;
        boundingBox = calculateBounds();
        boundingSphere = calculateBoundingSphere(boundingBox);
        storageInfo = new MeshStorageInfo();
    }

    public abstract Class<? extends Mesh> type();

    @Override
    public String name() {
        return name;
    }

    @Override
    public int handle() {
        return handle;
    }

    public MeshStorageInfo storageInfo() {
        return storageInfo;
    }

    public int vertexCount() {
        return vertexData.capacity() / stride;
    }

    public boolean indexed() {
        return indexData != null;
    }

    public int indexCount() {
        return indexed() ? indexData.capacity() / UINT32_SIZEOF : 0;
    }

    public IAABB bounds() {
        return boundingBox;
    }

    public ISphere boundingSphere() {
        return boundingSphere;
    }

    public int stride() {
        return stride;
    }

    public Vector3f position(int index, Vector3f dest) {
        return dest.set((index * stride) + VERTEX_POSITION_OFFSET, vertexData);
    }

    public Vector3f normal(int index, Vector3f dest) {
        return dest.set((index * stride) + VERTEX_NORMAL_OFFSET, vertexData);
    }

    public Vector2f texCoords(int index, Vector2f dest) {
        return dest.set((index * stride) + VERTEX_TEXCOORDS_OFFSET, vertexData);
    }

    public ByteBuffer vertexData() {
        return vertexData;
    }

    public ByteBuffer indexData() {
        return indexData;
    }

    private AABB calculateBounds() {

        AABB aabb = new AABB();

        Vector3f min = aabb.min;
        Vector3f max = aabb.max;

        Vector3f vertexPosition = new Vector3f();

        final int vertexCount = vertexCount();

        for(int i = 0;i < vertexCount;i++) {

            position(i, vertexPosition);

            min.x = min(min.x, vertexPosition.x);
            min.y = min(min.y, vertexPosition.y);
            min.z = min(min.z, vertexPosition.z);

            max.x = max(max.x, vertexPosition.x);
            max.y = max(max.y, vertexPosition.y);
            max.z = max(max.z, vertexPosition.z);
        }

        return aabb;
    }

    private Sphere calculateBoundingSphere(IAABB bounds) {

        Vector3f centroid = new Vector3f();

        centroid.x = bounds.min().x() + (bounds.max().x() - bounds.min().x()) / 2.0f;
        centroid.y = bounds.min().y() + (bounds.max().y() - bounds.min().y()) / 2.0f;
        centroid.z = bounds.min().z() + (bounds.max().z() - bounds.min().z()) / 2.0f;

        Sphere boundingSphere = new Sphere();

        boundingSphere.center.set(centroid);

        boundingSphere.radius = max(
                max((bounds.max().x()-bounds.min().x())/2, (bounds.max().y() - bounds.min().y())/2),
                (bounds.max().z() - bounds.min().z())/2);

        return boundingSphere;
    }

    @Override
    protected void free() {
        memFree(vertexData);
        memFree(indexData);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mesh mesh = (Mesh) o;
        return handle == mesh.handle;
    }

    @Override
    public int hashCode() {
        return handle;
    }

    @Override
    public String toString() {
        return "Mesh{" +
                "handle=" + handle +
                ", name='" + name + '\'' +
                ", vertexData=" + vertexData +
                ", indexData=" + indexData +
                ", boundingSphere=" + boundingSphere +
                '}';
    }
}
