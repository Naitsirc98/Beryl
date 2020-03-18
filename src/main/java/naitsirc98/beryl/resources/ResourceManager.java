package naitsirc98.beryl.resources;

import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.util.types.Singleton;
import org.lwjgl.system.NativeResource;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public final class ResourceManager extends BerylSystem {

    @Singleton
    private static ResourceManager instance;

    public static boolean contains(Object key) {
        return instance.resources.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public static <T extends NativeResource> T get(Object key) {
        return (T) instance.resources.get(requireNonNull(key));
    }

    public static void track(Object key, NativeResource resource) {
        instance.resources.put(requireNonNull(key), requireNonNull(resource));
    }

    public static void untrack(Object key) {
        instance.resources.remove(key);
    }

    public static void free(Object key) {
        if(instance.resources.containsKey(key)) {
            instance.resources.remove(key).free();
        }
    }

    private final Map<Object, NativeResource> resources;

    private ResourceManager() {
        resources = new HashMap<>();
    }

    @Override
    protected void init() {

    }

    @Override
    protected void terminate() {
        resources.values().forEach(NativeResource::free);
        resources.clear();
    }
}
