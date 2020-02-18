package naitsirc98.beryl.util;

public enum DataType implements ByteSize {

    INT8(1),
    UINT8(1),
    INT16(2),
    UINT16(2),
    INT32(4),
    UINT32(4),
    INT64(8),
    UINT64(8),
    FLOAT16(2),
    FLOAT24(3),
    FLOAT32(4),
    DOUBLE(8);

    private final byte bytes;

    DataType(int bytes) {
        this.bytes = (byte) bytes;
    }

    @Override
    public int sizeof() {
        return bytes;
    }

    public boolean isDecimal() {
        return this == FLOAT16 || this == FLOAT24 || this == FLOAT32 || this == DOUBLE;
    }
}
