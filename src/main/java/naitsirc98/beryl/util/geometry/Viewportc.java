package naitsirc98.beryl.util.geometry;

import static org.joml.Math.max;

public interface Viewportc {

    int x();

    int y();

    int width();

    int height();

    default float aspect() {
        return width() / max(height(), 1.0f);
    }

}
