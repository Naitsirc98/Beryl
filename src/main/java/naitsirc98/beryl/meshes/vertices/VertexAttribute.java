package naitsirc98.beryl.meshes.vertices;

import naitsirc98.beryl.util.types.ByteSize;
import naitsirc98.beryl.util.types.DataType;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.types.DataType.FLOAT32;

public enum VertexAttribute implements ByteSize {

    POSITION2D(FLOAT32, 2),
    POSITION3D(FLOAT32, 3),
    POSITION4D(FLOAT32, 4),
    NORMAL(FLOAT32, 3),
    TEXCOORDS(FLOAT32, 2),
    TANGENT(FLOAT32, 3),
    BITANGENT(FLOAT32, 3),
    COLOR(FLOAT32, 3),
    COLOR_ALPHA(FLOAT32, 4);

    private final DataType dataType;
    private final int size;

    VertexAttribute(DataType dataType, int size) {
        this.dataType = requireNonNull(dataType);
        this.size = size;
    }

    public DataType dataType() {
        return dataType;
    }

    public int size() {
        return size;
    }

    @Override
    public int sizeof() {
        return size * dataType.sizeof();
    }
}
