package naitsirc98.beryl.meshes.vertices;

import naitsirc98.beryl.util.types.ByteSize;
import naitsirc98.beryl.util.types.DataType;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.types.DataType.FLOAT32;

public final class VertexAttribute implements ByteSize {

    public static final VertexAttribute POSITION2D = new VertexAttribute(FLOAT32, 2);
    public static final VertexAttribute POSITION3D = new VertexAttribute(FLOAT32, 3);
    public static final VertexAttribute POSITION4D = new VertexAttribute(FLOAT32, 4);
    public static final VertexAttribute NORMAL = new VertexAttribute(FLOAT32, 3);
    public static final VertexAttribute TEXCOORDS = new VertexAttribute(FLOAT32, 2);
    public static final VertexAttribute COLOR = new VertexAttribute(FLOAT32, 3);
    public static final VertexAttribute COLOR_ALPHA = new VertexAttribute(FLOAT32, 4);


    private final DataType dataType;
    private final int size;

    public VertexAttribute(DataType dataType, int size) {
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

    public static final class Builder {

        private DataType dataType;
        private int size;

        public Builder dataType(DataType dataType) {
            this.dataType = dataType;
            return this;
        }

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public VertexAttribute build() {
            return new VertexAttribute(dataType, size);
        }
    }
}
