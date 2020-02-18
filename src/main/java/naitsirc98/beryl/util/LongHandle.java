package naitsirc98.beryl.util;

public interface LongHandle {

    long NULL = 0;

    long handle();

    default boolean isNull() {
        return handle() == NULL;
    }
}
