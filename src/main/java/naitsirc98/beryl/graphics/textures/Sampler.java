package naitsirc98.beryl.graphics.textures;

public interface Sampler {

    default void wrapMode(WrapMode wrapMode) {
        wrapModeS(wrapMode);
        wrapModeT(wrapMode);
        wrapModeR(wrapMode);
    }

    void wrapModeS(WrapMode wrapMode);
    void wrapModeT(WrapMode wrapMode);
    void wrapModeR(WrapMode wrapMode);

    default void filter(Filter filter) {
        minFilter(filter);
        magFilter(filter);
    }

    void minFilter(Filter filter);
    void magFilter(Filter filter);

    enum WrapMode {

        REPEAT,
        MIRRORED_REPEAT,
        CLAMP_TO_BORDER,
        CLAMP_TO_EDGE

    }

    enum Filter {

        NEAREST,
        LINEAR,
        MIPMAP_NEAREST,
        MIPMAP_LINEAR

    }
}
