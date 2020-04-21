package naitsirc98.beryl.util;

import naitsirc98.beryl.util.types.ByteSize;

import java.nio.ByteBuffer;

import static naitsirc98.beryl.util.types.DataType.VECTOR4_SIZEOF;

@ByteSize.Static(IColor.SIZEOF)
public interface IColor extends ByteSize {

    int SIZEOF = VECTOR4_SIZEOF;

    static Color intensified(IColor color, float factor) {
        return new Color(color).intensify(factor);
    }

    float red();
    float green();
    float blue();
    float alpha();

    ByteBuffer getRGB(ByteBuffer buffer);
    ByteBuffer getRGB(int pos, ByteBuffer buffer);
    ByteBuffer getRGBA(ByteBuffer buffer);
    ByteBuffer getRGBA(int pos, ByteBuffer buffer);

    @Override
    default int sizeof() {
        return SIZEOF;
    }

    default Color copy() {
        return new Color(red(), green(), blue(), alpha());
    }
}
