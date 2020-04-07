package naitsirc98.beryl.meshes.v2;

import java.nio.ByteBuffer;

import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;

public final class StaticMesh extends Mesh {

    public static final int VERTEX_DATA_SIZE = (3 + 3 + 2) * FLOAT32_SIZEOF;

    public StaticMesh(ByteBuffer vertexData) {
        super(vertexData, VERTEX_DATA_SIZE);
    }
}
