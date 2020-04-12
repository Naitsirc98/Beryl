package naitsirc98.beryl.resources;

public interface Resource extends AutoCloseable {

    default String name() {
        return "UNNAMED";
    }

    default boolean released() {
        return false;
    }

    void release();

    @Override
    default void close() {
        if(!released()) {
            release();
        }
    }
}
