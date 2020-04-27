package naitsirc98.beryl.meshes;

import org.joml.Vector2f;
import org.joml.Vector3f;

import static java.lang.Math.floor;

public class HeightMap {

    private final float[][] heights;
    private final float terrainSize;
    private final float minY;
    private final float maxY;

    public HeightMap(float terrainSize, float[][] heights, float minY, float maxY) {
        this.terrainSize = terrainSize;
        this.heights = heights;
        this.minY = minY;
        this.maxY = maxY;
    }

    public int rows() {
        return heights.length;
    }

    public int columns() {
        return heights[0].length;
    }

    public float[][] heightMap() {
        return heights;
    }

    public float minY() {
        return minY;
    }

    public float maxY() {
        return maxY;
    }

    public float heightAt(float worldX, float worldY) {
        return heightAt(0, 0, worldX, worldY);
    }

    public float heightAt(float terrainX, float terrainZ, float worldX, float worldZ) {

        final float deltaX = worldX - terrainX;
        final float deltaZ = worldZ - terrainZ;

        final float gridSize = terrainSize / (float) (rows() - 1);

        final int gridX = (int) floor(deltaX / gridSize);
        final int gridZ = (int) floor(deltaZ / gridSize);

        if(gridX < 0 || gridZ < 0 || gridX >= rows() - 1 || gridZ >= rows() - 1) {
            return 0.0f;
        }

        final float x = (deltaX % gridSize) / gridSize;
        final float z = (deltaZ % gridSize) / gridSize;

        if(x <= (1 - z)) {
            return barryCentric(0, heights[gridX][gridZ], 0,
                    1, heights[gridX + 1][gridZ], 0,
                    0, heights[gridX][gridZ + 1], 1,
                    x, z);
        }

        return barryCentric(1, heights[gridX + 1][gridZ], 0,
                1, heights[gridX + 1][gridZ + 1], 1,
                0, heights[gridX][gridZ + 1], 1,
                x, z);
    }

    private float barryCentric(Vector3f p1, Vector3f p2, Vector3f p3, Vector2f pos) {

        final float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
        final float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
        final float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
        final float l3 = 1.0f - l1 - l2;

        return l1 * p1.y + l2 * p2.y + l3 * p3.y;
    }

    private float barryCentric(float x1, float y1, float z1,
                                     float x2, float y2, float z2,
                                     float x3, float y3, float z3,
                                     float x, float z) {

        final float det = (z2 - z3) * (x1 - x3) + (x3 - x2) * (z1 -z3);
        final float l1 = ((z2 - z3) * (x - x3) + (x3 - x2) * (z - z3)) / det;
        final float l2 = ((z3 - z1) * (x - x3) + (x1 - x3) * (z - z3)) / det;
        final float l3 = 1.0f - l1 - l2;

        return l1 * y1 + l2 * y2 + l3 * y3;
    }
}
