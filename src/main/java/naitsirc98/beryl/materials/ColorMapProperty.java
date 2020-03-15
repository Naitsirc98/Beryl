package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.types.ByteSize;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.types.DataType.INT32_SIZEOF;

@ByteSize.Static(ColorMapProperty.SIZEOF)
public class ColorMapProperty implements ByteSize {

    public static final int SIZEOF = Color.SIZEOF + INT32_SIZEOF;

    private Color color;
    private Texture2D map;

    public ColorMapProperty() {
        color = Color.WHITE;
        map = GraphicsFactory.get().blankTexture2D();
    }

    public Color color() {
        return color;
    }

    public ColorMapProperty color(Color color) {
        this.color = requireNonNull(color);
        return this;
    }

    public Texture2D map() {
        return map;
    }

    public ColorMapProperty map(Texture2D map) {
        this.map = requireNonNull(map);
        return this;
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }
}
