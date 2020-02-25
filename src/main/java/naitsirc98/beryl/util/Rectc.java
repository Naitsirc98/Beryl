package naitsirc98.beryl.util;

import static java.lang.Math.max;

public interface Rectc {
    
    int left();

    default int x() {
        return left();
    }

    int right();

    default int width() {
        return right();
    }

    int top();

    default int y() {
        return top();
    }

    int bottom();

    default int height() {
        return bottom();
    }

    default float aspect() {
        return width() / max(height(), 1.0f);
    }
}
