package naitsirc98.beryl.util.handles;

/**
 * Interface for those objects that wraps a long handle
 */
public interface LongHandle {

    /**
     * The NULL handle.
     */
    long NULL = 0;

    /**
     * Returns the handle of this object
     *
     * @return the handle
     */
    long handle();

    /**
     * Tells whether this object has a null handle or not
     *
     * @return if this object has a null handle
     */
    default boolean isNull() {
        return handle() == NULL;
    }
}
