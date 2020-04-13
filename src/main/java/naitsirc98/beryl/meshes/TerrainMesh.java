package naitsirc98.beryl.meshes;

import naitsirc98.beryl.util.Maths;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import static java.lang.Math.floor;
import static naitsirc98.beryl.util.Maths.barryCentric;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;

public final class TerrainMesh extends StaticMesh {

    public static final int VERTEX_DATA_SIZE = (3 + 3 + 2) * FLOAT32_SIZEOF;

    private final float size;
    private final float[][] heightMap;
    private final float minY;
    private final float maxY;

    TerrainMesh(int handle, String name, ByteBuffer vertexData, ByteBuffer indexData, float size, float[][] heightMap, float minY, float maxY) {
        super(handle, name, vertexData, indexData);
        this.size = size;
        this.heightMap = heightMap;
        this.minY = minY;
        this.maxY = maxY;
        boundingSphere.radius *= 1.5f;
    }

    public float size() {
        return size;
    }

    public float minY() {
        return minY;
    }

    public float maxY() {
        return maxY;
    }

    public float heightAt(float terrainX, float terrainZ, float worldX, float worldZ) {

        final float deltaX = worldX - terrainX;
        final float deltaZ = worldZ - terrainZ;

        final float gridSize = size / (float) (heightMap.length - 1);

        final int gridX = (int) floor(deltaX / gridSize);
        final int gridZ = (int) floor(deltaZ / gridSize);

        if(gridX < 0 || gridZ < 0 || gridX >= heightMap.length - 1 || gridZ >= heightMap.length - 1) {
            return 0.0f;
        }

        final float x = (deltaX % gridSize) / gridSize;
        final float z = (deltaZ % gridSize) / gridSize;

        return x <= (1 - z)
                ?
                barryCentric(0, heightMap[gridX][gridZ], 0,
                        1, heightMap[gridX + 1][gridZ], 0,
                        0, heightMap[gridX][gridZ + 1], 1,
                        x, z)
                :
                barryCentric(1, heightMap[gridX + 1][gridZ], 0,
                        1, heightMap[gridX + 1][gridZ + 1], 1,
                        0, heightMap[gridX][gridZ + 1], 1,
                        x, z);
    }
}
