package naitsirc98.beryl.materials;

import naitsirc98.beryl.logging.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class Materials {

    private static final Map<String, Material> MATERIALS = new HashMap<>();
    private static final AtomicInteger HASHCODE = new AtomicInteger(0);

    public static boolean exists(String name) {
        return MATERIALS.containsKey(name);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Material> T get(String name) {
        return (T) MATERIALS.get(name);
    }

    static int nextHashCode() {
        return HASHCODE.getAndIncrement();
    }

    static synchronized void register(Material material) {

        if(MATERIALS.containsKey(material.name())) {
            Log.error("There is already a material called " + material.name());
            return;
        }

        MATERIALS.put(material.name(), material);
    }

}
