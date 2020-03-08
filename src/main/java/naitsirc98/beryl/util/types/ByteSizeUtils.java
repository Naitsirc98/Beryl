package naitsirc98.beryl.util.types;

import java.util.Map;
import java.util.WeakHashMap;

public final class ByteSizeUtils {

    private static final Map<Class<?>, Integer> SIZEOF_CACHE = new WeakHashMap<>();

    public static int sizeof(ByteSize object) {
        return object.sizeof();
    }

    public static int sizeof(Object object) {
        return sizeof(object.getClass());
    }

    public static int sizeof(Class<?> type) {

        if(SIZEOF_CACHE.containsKey(type)) {
            return SIZEOF_CACHE.get(type);
        }

        final ByteSize.Static byteSize = type.getAnnotation(ByteSize.Static.class);

        if(byteSize != null) {
            final int sizeof = byteSize.value();
            cacheSize(type, sizeof);
            return sizeof;
        }

        throw new IllegalArgumentException("Type " + type.getSimpleName() + " does not implement ByteSize.Static annotation");
    }

    private static synchronized void cacheSize(Class<?> type, int sizeof) {
        SIZEOF_CACHE.put(type, sizeof);
    }

    private ByteSizeUtils() {}
}
