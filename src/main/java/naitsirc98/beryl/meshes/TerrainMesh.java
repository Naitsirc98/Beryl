package naitsirc98.beryl.meshes;

import java.nio.ByteBuffer;

import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;

public final class TerrainMesh extends StaticMesh {

    public static final int VERTEX_DATA_SIZE = (3 + 3 + 2) * FLOAT32_SIZEOF;

    private final HeightMap heightMap;

    TerrainMesh(int handle, String name, ByteBuffer vertexData, ByteBuffer indexData, HeightMap heightMap) {
        super(handle, name, vertexData, indexData);
        this.heightMap = heightMap;
        boundingSphere.radius *= 1.5f;
    }

    public HeightMap heightMap() {
        return heightMap;
    }

    @Override
    public Class<? extends Mesh> type() {
        return StaticMesh.class;
    }
}
