package naitsirc98.beryl.util.geometry;

import naitsirc98.beryl.util.types.ByteSize;
import org.joml.Vector3fc;

import java.nio.ByteBuffer;

import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;

@ByteSize.Static(ISphere.SIZEOF)
public interface ISphere extends ByteSize {

    int SIZEOF = 4 * FLOAT32_SIZEOF;

    float centerX();
    float centerY();
    float centerZ();

    Vector3fc center();

    float radius();

    default int sizeof() {
        return SIZEOF;
    }

    ByteBuffer get(int offset, ByteBuffer buffer);
}
