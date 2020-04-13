package naitsirc98.beryl.meshes;

import naitsirc98.beryl.images.Image;
import naitsirc98.beryl.images.ImageFactory;
import naitsirc98.beryl.images.PixelFormat;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static naitsirc98.beryl.meshes.Mesh.*;
import static naitsirc98.beryl.meshes.TerrainMesh.VERTEX_DATA_SIZE;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.INT32_SIZEOF;
import static org.lwjgl.system.MemoryUtil.memCalloc;

public final class TerrainMeshLoader {

    private static final float MAX_VALUE = 255 * 255 * 255;

    private static final float DEFAULT_MIN_HEIGHT = -50;
    private static final float DEFAULT_MAX_HEIGHT = 50;

    private static final TerrainMeshLoader INSTANCE = new TerrainMeshLoader();

    public static TerrainMeshLoader get() {
        return INSTANCE;
    }


    private final Map<String, TerrainMesh> cache;

    private TerrainMeshLoader() {
        cache = new HashMap<>();
    }

    public TerrainMesh load(String name, String heightMapPath, float size) {
        return load(name, heightMapPath, size, DEFAULT_MIN_HEIGHT, DEFAULT_MAX_HEIGHT);
    }

    public TerrainMesh load(String name, String heightMapPath, float size, float minY, float maxY) {

        if(cache.containsKey(name)) {
            TerrainMesh mesh = cache.get(name);
            if(mesh.released()) {
                cache.remove(name);
            } else {
                return mesh;
            }
        }

        try(Image heightMapImage = ImageFactory.newImage(heightMapPath, PixelFormat.RGBA)) {

            return generateTerrain(name, size, minY, maxY, heightMapImage.width(), heightMapImage.height(), heightMapImage.pixelsi());
        }
    }

    private TerrainMesh generateTerrain(String name, float size, float minY, float maxY, int width, int height, ByteBuffer pixels) {

        float[][] heightMap = new float[height][width];

        final float incX = size / (width - 1);
        final float incZ = size / (height - 1);

        ByteBuffer vertices = memCalloc(width * height * VERTEX_DATA_SIZE);
        ByteBuffer indices = memCalloc((width - 1) * (height - 1) * 6 * INT32_SIZEOF);

        int positionsOffset = VERTEX_POSITION_OFFSET;
        int normalsOffset = VERTEX_NORMAL_OFFSET;
        int textCoordsOffset = VERTEX_TEXCOORDS_OFFSET;

        for(int row = 0; row < height; row++) {

            for(int column = 0; column < width; column++) {

                final float currentHeight = getHeightAt(minY, maxY, column, row, width, pixels);
                heightMap[column][row] = currentHeight;

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

        return MeshManager.get().createTerrainMesh(name, vertices, indices, size, heightMap, minY, maxY);
    }

    private void setNormal(ByteBuffer vertices, int offset,
                           int row, int column,
                           float minY, float maxY,
                           int width, ByteBuffer pixels) {

        // Finite difference method

        final float heightL = getHeightAt(minY, maxY, column - 1, row, width, pixels);
        final float heightR = getHeightAt(minY, maxY, column + 1, row, width, pixels);
        final float heightD = getHeightAt(minY, maxY, column, row - 1, width, pixels);
        final float heightU = getHeightAt(minY, maxY, column, row + 1, width, pixels);

        vertices.putFloat(offset, heightL - heightR)
                .putFloat(offset + FLOAT32_SIZEOF, 2.0f)
                .putFloat(offset + 2 * FLOAT32_SIZEOF, heightD - heightU);
    }

    private void setIndices(int width, ByteBuffer indices, int row, int column) {

        final int leftTop = row * width + column;
        final int rightTop = leftTop + 1;
        final int leftBottom = (row + 1) * width + column;
        final int rightBottom = leftBottom + 1;

        indices.putInt(leftTop)
                .putInt(leftBottom)
                .putInt(rightTop);

        indices.putInt(rightTop)
                .putInt(leftBottom)
                .putInt(rightBottom);
    }

    private void setPosition(ByteBuffer vertices, int offset, float currentHeight, int row, int column, float incX, float incZ) {

        final float x = column * incX;
        final float y = currentHeight;
        final float z = row * incZ;

        vertices.putFloat(offset, x)
                .putFloat(offset + FLOAT32_SIZEOF, y)
                .putFloat(offset + 2 * FLOAT32_SIZEOF, z);
    }

    private void setTextureCoordinates(ByteBuffer vertices, int offset, int row, int column, int width, int height) {

        final float u = (float) column / (float) width;
        final float v = (float) row / (float) height;

        vertices.putFloat(offset, u)
                .putFloat(offset + FLOAT32_SIZEOF, v);
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
        final byte a = 0;//pixels.get(x * 4 + 3 + z * 4 * width);

        return ((0xFF & a) << 24) | ((0xFF & r) << 16)
                | ((0xFF & g) << 8) | (0xFF & b);
    }
}
