package naitsirc98.beryl.resources;

public interface Resource extends AutoCloseable {

    static void release(Resource resource) {
        if(resource != null && !resource.released()) {
            resource.release();
        }
    }

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
