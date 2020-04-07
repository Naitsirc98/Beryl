package naitsirc98.beryl.util.geometry;

import naitsirc98.beryl.util.types.ByteSize;
import org.joml.Vector3fc;

import java.nio.ByteBuffer;

public interface IBounds extends ByteSize {

    Vector3fc min();

    Vector3fc max();

    float centerX();
    float centerY();
    float centerZ();

    ByteBuffer get(int offset, ByteBuffer buffer);
}
