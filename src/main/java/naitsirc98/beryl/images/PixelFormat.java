package naitsirc98.beryl.images;

import naitsirc98.beryl.util.types.ByteSize;
import naitsirc98.beryl.util.types.DataType;

import java.nio.Buffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static naitsirc98.beryl.util.types.DataType.*;

public enum PixelFormat implements ByteSize {

    RED(1, UINT8),
    RG(2, UINT8),
    RGB(3, UINT8),
    RGBA(4, UINT8),

    RED16F(1, FLOAT16),
    RG16F(2, FLOAT16),
    RGB16F(3, FLOAT16),
    RGBA16F(4, FLOAT16),

    RED32F(1, FLOAT32),
    RG32F(2, FLOAT32),
    RGB32F(3, FLOAT32),
    RGBA32F(4, FLOAT32);

    private final byte channels;
    private final DataType dataType;

    PixelFormat(int channels, DataType dataType) {
        this.channels = (byte) channels;
        this.dataType = dataType;
    }

    public static PixelFormat of(Buffer pixels, int channels) {

        assertNonNull(pixels);
        assertTrue(channels > 0 && channels <= 4);

        if(pixels instanceof FloatBuffer || pixels instanceof DoubleBuffer) {
            return floatFormatFromChannels(channels);
        } else {
            return intFormatFromChannels(channels);
        }
    }

    private static PixelFormat intFormatFromChannels(int channels) {
        switch(channels) {
            case 1:
                return RED;
            case 2:
                return RG;
            case 3:
                return RGB;
            case 4:
                return RGBA;
        }
        throw new IllegalArgumentException();
    }

    private static PixelFormat floatFormatFromChannels(int channels) {
        switch(channels) {
            case 1:
                return RED32F;
            case 2:
                return RG32F;
            case 3:
                return RGB32F;
            case 4:
                return RGBA32F;
        }
        throw new IllegalArgumentException();
    }

    public byte channels() {
        return channels;
    }

    public DataType dataType() {
        return dataType;
    }

    @Override
    public int sizeof() {
        return channels * dataType.sizeof();
    }
}
