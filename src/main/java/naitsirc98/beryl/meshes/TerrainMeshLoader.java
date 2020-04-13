package naitsirc98.beryl.meshes;

import naitsirc98.beryl.images.Image;
import naitsirc98.beryl.images.ImageFactory;
import naitsirc98.beryl.images.PixelFormat;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static naitsirc98.beryl.meshes.Mesh.*;
import static naitsirc98.beryl.meshes.TerrainMesh.VERTEX_DATA_SIZE;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.INT32_SIZEOF;
import static org.lwjgl.system.MemoryUtil.memCalloc;

public final class TerrainMeshLoader {

    private static final float MAX_VALUE = 255 * 255 * 255;

    public static final float START_X = -0.5f;
    public static final float START_Z = -0.5f;

    public static final float LENGTH_X = -START_X * 2.0f;
    public static final float LENGTH_Z = -START_Z * 2.0f;


    private static final TerrainMeshLoader INSTANCE = new TerrainMeshLoader();

    public static TerrainMeshLoader get() {
        return INSTANCE;
    }


    private final Map<String, TerrainMesh> cache;

    private TerrainMeshLoader() {
        cache = new HashMap<>();
    }

    public TerrainMesh load(String name, String heightMapPath) {
        return load(name, heightMapPath, 0, MAX_VALUE);
    }

    public TerrainMesh load(String name, String heightMapPath, float minY, float maxY) {

        if(cache.containsKey(name)) {
            TerrainMesh mesh = cache.get(name);
            if(mesh.released()) {
                cache.remove(name);
            } else {
                return mesh;
            }
        }

        try(Image heightMapImage = ImageFactory.newImage(heightMapPath, PixelFormat.RGBA)) {

            return generateTerrain(name, minY, maxY, heightMapImage.width(), heightMapImage.height(), heightMapImage.pixelsi());
        }
    }

    private TerrainMesh generateTerrain(String name, float minY, float maxY, int width, int height, ByteBuffer pixels) {

        float[][] heightMap = new float[height][width];

        final float incX = LENGTH_X / (width - 1);
        final float incZ = LENGTH_Z / (height - 1);

        ByteBuffer vertices = memCalloc(width * height * VERTEX_DATA_SIZE);
        ByteBuffer indices = memCalloc((width - 1) * (height - 1) * 6 * INT32_SIZEOF);

        int positionsOffset = VERTEX_POSITION_OFFSET;
        int normalsOffset = VERTEX_NORMAL_OFFSET;
        int textCoordsOffset = VERTEX_TEXCOORDS_OFFSET;

        for(int row = 0; row < height; row++) {

            for(int column = 0; column < width; column++) {

                final float currentHeight = getHeightAt(minY, maxY, column, row, width, pixels);
                heightMap[row][column] = currentHeight;

                // Position
                setPosition(vertices, positionsOffset, currentHeight, row, column, incX, incZ);
                positionsOffset += VERTEX_DATA_SIZE;

                // Normal
                setNormal(vertices, normalsOffset, row, column, minY, maxY, width, pixels);
                normalsOffset += VERTEX_DATA_SIZE;

                // Set texture coordinates
                setTextureCoordinates(vertices, textCoordsOffset, row, column, width, height);
                textCoordsOffset += VERTEX_DATA_SIZE;

                // Create indices
                if(column < width - 1 && row < height - 1) {
                    setIndices(width, indices, row, column);
                }
            }
        }

        vertices.rewind();
        indices.rewind();

        return MeshManager.get().createTerrainMesh(name, vertices, indices, heightMap, minY, maxY);
    }

    private void setNormal(ByteBuffer vertices, int normalsOffset,
                           int row, int column,
                           float minY, float maxY,
                           int width, ByteBuffer pixels) {

        // Finite difference method

        final float heightL = getHeightAt(minY, maxY, column - 1, row, width, pixels);
        final float heightR = getHeightAt(minY, maxY, column + 1, row, width, pixels);
        final float heightD = getHeightAt(minY, maxY, column, row - 1, width, pixels);
        final float heightU = getHeightAt(minY, maxY, column, row + 1, width, pixels);

        vertices.putFloat(normalsOffset, heightL - heightR)
                .putFloat(normalsOffset + FLOAT32_SIZEOF, 2.0f)
                .putFloat(normalsOffset + 2 * FLOAT32_SIZEOF, heightD - heightU);
    }

    private void setIndices(int width, ByteBuffer indices, int row, int column) {

        final int leftTop = row * width + column;
        final int leftBottom = (row + 1) * width + column;
        final int rightBottom = (row + 1) * width + column + 1;
        final int rightTop = row * width + column + 1;

        indices.putInt(rightTop)
                .putInt(leftBottom)
                .putInt(rightBottom);

        indices.putInt(leftTop)
                .putInt(leftBottom)
                .putInt(rightTop);
    }

    private void setPosition(ByteBuffer vertices, int offset, float currentHeight, int row, int column, float incX, float incZ) {

        final float x = START_X + column * incX;
        final float y = currentHeight;
        final float z = START_Z + row * incZ;

        vertices.putFloat(offset, x)
                .putFloat(offset + FLOAT32_SIZEOF, y)
                .putFloat(offset + 2 * FLOAT32_SIZEOF, z);
    }

    private void setTextureCoordinates(ByteBuffer vertices, int textCoordsOffset, int row, int column, int width, int height) {

        final float u = (float) column / (float) width;
        final float v = (float) row / (float) height;

        vertices.putFloat(textCoordsOffset, u)
                .putFloat(textCoordsOffset + FLOAT32_SIZEOF, v);
    }

    private void calculateTerrainNormals(ByteBuffer vertices, int width, int height) {

        Vertex vertex = new Vertex(vertices);

        VertexSurroundings grid = new VertexSurroundings();

        Vector3f normal = new Vector3f();

        int normalsOffset = VERTEX_NORMAL_OFFSET;

        for(int row = 0; row < height; row++) {

            for(int column = 0; column < width; column++) {

                System.out.println(row + ", " + column);

                if(row > 0 && row < height - 1 && column > 0 && column < width - 1) {

                    getSurroundingVertices(vertex, row, column, width, grid);

                    grid.calculateNormals();

                    grid.getNormal(normal);

                } else {
                    normal.set(0, 1, 0);
                }

                normal.get(normalsOffset, vertices);

                normalsOffset += VERTEX_DATA_SIZE;
            }
        }
    }

    private void getSurroundingVertices(Vertex vertex, int row, int column, int width, VertexSurroundings surroundings) {

        final Vector3f v0 = surroundings.v0;
        final Vector3f v1 = surroundings.v1;
        final Vector3f v2 = surroundings.v2;
        final Vector3f v3 = surroundings.v3;
        final Vector3f v4 = surroundings.v4;

        final int i0 = row * width * 3 + column * 3;
        vertex.position(i0, v0);

        final int i1 = row * width * 3 + (column - 1) * 3;
        vertex.position(i1, v1);
        v1.sub(v0);

        final int i2 = (row + 1) * width * 3 + column * 3;
        vertex.position(i2, v2);
        v2.sub(v0);

        final int i3 = row * width * 3 + (column + 1) * 3;
        vertex.position(i3, v3);
        v3.sub(v0);

        final int i4 = (row - 1) * width * 3 + column * 3;
        vertex.position(i4, v4);
        v4.sub(v0);
    }

    private float getHeightAt(float minY, float maxY, int x, int z, int width, ByteBuffer pixels) {
        final float argb = getARGB(x, z, width, pixels);
        return minY + Math.abs(maxY - minY) * (argb / MAX_VALUE);
    }

    public static int getARGB(int x, int z, int width, ByteBuffer pixels) {

        if(x < 0 || x >= width || z < 0 || z >= width) {
            return 0;
        }

        final byte r = pixels.get(x * 4 + 0 + z * 4 * width);
        final byte g = pixels.get(x * 4 + 1 + z * 4 * width);
        final byte b = pixels.get(x * 4 + 2 + z * 4 * width);
        final byte a = pixels.get(x * 4 + 3 + z * 4 * width);

        return ((0xFF & a) << 24) | ((0xFF & r) << 16)
                | ((0xFF & g) << 8) | (0xFF & b);
    }

    private static final class VertexSurroundings {

        private final Vector3f v0 = new Vector3f();
        private final Vector3f v1 = new Vector3f();
        private final Vector3f v2 = new Vector3f();
        private final Vector3f v3 = new Vector3f();
        private final Vector3f v4 = new Vector3f();

        private final Vector3f v12 = new Vector3f();
        private final Vector3f v23 = new Vector3f();
        private final Vector3f v34 = new Vector3f();
        private final Vector3f v41 = new Vector3f();

        private VertexSurroundings() {

        }

        public void calculateNormals() {

            v1.cross(v2, v12).normalize();

            v2.cross(v3, v23).normalize();

            v3.cross(v4, v34).normalize();

            v4.cross(v1, v41).normalize();
        }

        public void getNormal(Vector3f dest) {
            v12.add(v23, dest).add(v34, dest).add(v41, dest).normalize();
        }
    }
}
