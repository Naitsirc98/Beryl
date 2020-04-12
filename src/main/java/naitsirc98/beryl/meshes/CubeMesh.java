package naitsirc98.beryl.meshes;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryUtil.memAlloc;

public final class CubeMesh {

    private static StaticMesh cubeMesh;

    public static StaticMesh get() {
        return cubeMesh;
    }

    static void create() {

        ByteBuffer vertices = getVertices();
        ByteBuffer indices = getIndices();

        cubeMesh = StaticMesh.get("CUBE", staticMeshData -> staticMeshData.set(vertices, indices));
    }


    private static ByteBuffer getIndices() {
        return null;
    }

    private static ByteBuffer getVertices() {

        ByteBuffer vertices = memAlloc(36 * StaticMesh.VERTEX_DATA_SIZE);

        StaticMesh.Vertex vertex = new StaticMesh.Vertex(vertices);

        final float unit = 0.5f;

        // Vertices are in counter clockwise order

        /* Front Face */ {

            vertex.position( unit,  unit, 0).normal(0, 0, -1).texCoords(1, 1);
            vertex.position(-unit,  unit, 0).normal(0, 0, -1).texCoords(0, 1);
            vertex.position(-unit, -unit, 0).normal(0, 0, -1).texCoords(0, 0);

            vertex.position( unit,  unit, 0).normal(0, 0, -1).texCoords(1, 1);
            vertex.position(-unit, -unit, 0).normal(0, 0, -1).texCoords(0, 0);
            vertex.position( unit, -unit, 0).normal(0, 0, -1).texCoords(1, 0);
        }

        /* Back Face */ {

            vertex.position( unit,  unit, -unit).normal(0, 0, 1).texCoords(1, 1);
            vertex.position(-unit,  unit, -unit).normal(0, 0, 1).texCoords(0, 1);
            vertex.position(-unit, -unit, -unit).normal(0, 0, 1).texCoords(0, 0);

            vertex.position( unit,  unit, -unit).normal(0, 0, 1).texCoords(1, 1);
            vertex.position(-unit, -unit, -unit).normal(0, 0, 1).texCoords(0, 0);
            vertex.position( unit, -unit, -unit).normal(0, 0, 1).texCoords(1, 0);
        }

        return null;
    }

    private CubeMesh() {}
}
