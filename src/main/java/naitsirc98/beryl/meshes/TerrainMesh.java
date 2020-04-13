package naitsirc98.beryl.meshes;

import java.nio.ByteBuffer;

import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;

public final class TerrainMesh extends Mesh {

    public static final int VERTEX_DATA_SIZE = (3 + 3 + 2) * FLOAT32_SIZEOF;

    private final float[][] heightMap;
    private final float minY;
    private final float maxY;

    TerrainMesh(int handle, String name, ByteBuffer vertexData, ByteBuffer indexData, float[][] heightMap, float minY, float maxY) {
        super(handle, name, vertexData, indexData, VERTEX_DATA_SIZE);
        this.heightMap = heightMap;
        this.minY = minY;
        this.maxY = maxY;
    }

    public float minY() {
        return minY;
    }

    public float maxY() {
        return maxY;
    }

    public float height(int row, int column) {
        if(row >= 0 && row < heightMap.length) {
            if(column >= 0 && column < heightMap[row].length) {
                return heightMap[row][column];
            }
        }
        return 0.0f;
    }
}
