package naitsirc98.beryl.meshes;

import naitsirc98.beryl.resources.ManagedResource;
import naitsirc98.beryl.util.geometry.Bounds;
import naitsirc98.beryl.util.geometry.IBounds;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.UINT32_SIZEOF;
import static org.lwjgl.system.MemoryUtil.memFree;

public abstract class Mesh extends ManagedResource {

    public static final int VERTEX_POSITION_OFFSET = 0;
    public static final int VERTEX_POSITION_SIZE = 3 * FLOAT32_SIZEOF;

    public static final int VERTEX_NORMAL_OFFSET = VERTEX_POSITION_SIZE;
    public static final int VERTEX_NORMAL_SIZE = 3 * FLOAT32_SIZEOF;

    public static final int VERTEX_TEXCOORDS_OFFSET = VERTEX_POSITION_SIZE + VERTEX_NORMAL_SIZE;
    public static final int VERTEX_TEXCOORDS_SIZE = 2 * FLOAT32_SIZEOF;

    private final ByteBuffer vertexData;
    private final ByteBuffer indexData;
    private final Bounds bounds;
    private final int vertexDataStride;

    public Mesh(ByteBuffer vertexData, int vertexDataStride) {
        this(vertexData, null, vertexDataStride);
    }

    public Mesh(ByteBuffer vertexData, ByteBuffer indexData, int vertexDataStride) {
        this.vertexData = requireNonNull(vertexData);
        this.indexData = requireNonNull(indexData);
        this.vertexDataStride = vertexDataStride;
        bounds = calculateBounds();
    }


    public int vertexCount() {
        return vertexData.remaining() / vertexDataStride;
    }

    public boolean indexed() {
        return indexData != null;
    }

    public int indexCount() {
        return indexed() ? indexData.remaining() / UINT32_SIZEOF : 0;
    }

    public IBounds bounds() {
        return bounds;
    }

    public Vector3f position(int index, Vector3f dest) {
        return dest.set((index * vertexDataStride) + VERTEX_POSITION_OFFSET, vertexData);
    }

    public Vector3f normal(int index, Vector3f dest) {
        return dest.set((index * vertexDataStride) + VERTEX_NORMAL_OFFSET, vertexData);
    }

    public Vector2f texCoords(int index, Vector2f dest) {
        return dest.set((index * vertexDataStride) + VERTEX_TEXCOORDS_OFFSET, vertexData);
    }

    public ByteBuffer vertexData() {
        return vertexData;
    }

    public ByteBuffer indexData() {
        return indexData;
    }


    private Bounds calculateBounds() {

        Bounds bounds = new Bounds();

        Vector3f min = bounds.min;
        Vector3f max = bounds.max;

        Vector3f vertexPosition = new Vector3f();

        final int vertexCount = vertexCount();

        for(int i = 0;i < vertexCount;i++) {

            position(i, vertexPosition);

            min.x = min(min.x, vertexPosition.x);
            min.x = min(min.y, vertexPosition.y);
            min.x = min(min.z, vertexPosition.z);

            max.x = max(max.x, vertexPosition.x);
            max.x = max(max.x, vertexPosition.x);
            max.x = max(max.x, vertexPosition.x);
        }

        return bounds;
    }

    @Override
    protected void free() {
        memFree(vertexData);
        memFree(indexData);
    }

}
