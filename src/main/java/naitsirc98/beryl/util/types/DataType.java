package naitsirc98.beryl.util.types;

public enum DataType implements ByteSize {

    INT8(1, true),
    UINT8(1, false),
    INT16(2, true),
    UINT16(2, false),
    INT32(4, true),
    UINT32(4, false),
    INT64(8, true),
    UINT64(8, false),
    FLOAT16(2, true),
    FLOAT32(4, true),
    DOUBLE(8, true);


    public static final int INT8_SIZEOF = 1;
    public static final int UINT8_SIZEOF = 1;
    public static final int INT16_SIZEOF = 2;
    public static final int UINT16_SIZEOF = 2;
    public static final int INT32_SIZEOF = 4;
    public static final int UINT32_SIZEOF = 4;
    public static final int INT64_SIZEOF = 8;
    public static final int UINT64_SIZEOF = 8;
    public static final int FLOAT16_SIZEOF = 2;
    public static final int FLOAT32_SIZEOF = 4;
    public static final int DOUBLE_SIZEOF = 8;

    public static final int VECTOR2_SIZEOF = 2 * FLOAT32_SIZEOF;
    public static final int VECTOR3_SIZEOF = 3 * FLOAT32_SIZEOF;
    public static final int VECTOR4_SIZEOF = 4 * FLOAT32_SIZEOF;
    public static final int MATRIX4_SIZEOF = 4 * 4 * FLOAT32_SIZEOF;

    public static final int SAMPLER_SIZEOF = UINT64_SIZEOF;

    public static final int INT32_MIN = Integer.MIN_VALUE;
    public static final int INT32_MAX = Integer.MAX_VALUE;

    public static final int UINT32_MIN = 0;
    public static final int UINT32_MAX = 0xFFFFFFFF;

    public static final long INT64_MIN = Long.MIN_VALUE;
    public static final long INT64_MAX = Long.MAX_VALUE;

    public static final long UINT64_MIN = 0;
    public static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;

    private final byte bytes;
    private final boolean signed;

    DataType(int bytes, boolean signed) {
        this.bytes = (byte) bytes;
        this.signed = signed;
    }

    public boolean signed() {
        return signed;
    }

    @Override
    public int sizeof() {
        return bytes;
    }


    public boolean decimal() {
        return this == FLOAT16 || this == FLOAT32 || this == DOUBLE;
    }
}
