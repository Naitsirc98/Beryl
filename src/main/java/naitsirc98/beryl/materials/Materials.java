package naitsirc98.beryl.materials;

import naitsirc98.beryl.logging.Log;

import java.util.HashMap;
import java.util.Map;

public final class Materials {

    private static final Map<String, Material> MATERIALS = new HashMap<>();
    private static int nextHashCode;

    public static boolean exists(String name) {
        return MATERIALS.containsKey(name);
    }

    public static Material get(String name) {
        return MATERIALS.get(name);
    }

    static synchronized int nextHashCode() {
        return nextHashCode++;
    }

    static synchronized void register(Material material) {

        if(MATERIALS.containsKey(material.name())) {
            Log.error("There is already a material called " + material.name());
            return;
        }

        MATERIALS.put(material.name(), material);
    }

}
