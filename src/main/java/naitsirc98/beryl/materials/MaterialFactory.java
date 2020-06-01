package naitsirc98.beryl.materials;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.tasks.Task;
import naitsirc98.beryl.tasks.TaskManager;
import naitsirc98.beryl.util.types.TypeUtils;

import java.util.function.Consumer;

public class MaterialFactory<T extends Material> {

    private final Class<T> materialClass;

    public MaterialFactory(Class<T> materialClass) {
        this.materialClass = materialClass;
    }

    public T getDefault() {
        return getMaterial(getDefaultName(), material -> {});
    }

    public boolean exists(String name) {
        return getUnchecked(name) != null;
    }

    public T getMaterial(String name) {

        T material = getUnchecked(name);

        if(material != null) {
            return material;
        }

        material = instantiateNewMaterial(name);

        return material;
    }

    public T getMaterial(String name, Consumer<T> initializer) {

        T material = getUnchecked(name);

        if(material != null) {
            return material;
        }

        material = instantiateNewMaterial(name);

        initializer.accept(material);

        return material;
    }

    protected String getDefaultName() {
        return materialClass.getSimpleName() + "_default";
    }

    private T getUnchecked(String name) {

        MaterialManager manager = MaterialManager.get();

        if(manager.exists(name)) {

            Material material = manager.get(name);

            if(material.getClass() == materialClass) {
                return materialClass.cast(material);
            }
        }

        return null;
    }

    private T instantiateNewMaterial(String name) {

        T material = TypeUtils.newInstance(materialClass, name);

        if(material == null) {
            Log.error("Could not instantiate material " + name + " of class " + materialClass, new RuntimeException());
            return getDefault();
        }

        MaterialManager.get().addMaterial(material);

        return material;
    }
}
