package naitsirc98.beryl.scenes.components.meshes;

import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.AbstractComponentManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MeshViewManager extends AbstractComponentManager<MeshView> {

    private final Map<Material, Integer> materials;

    private MeshViewManager(Scene scene) {
        super(scene);
        materials = new HashMap<>();
    }

    public List<MeshView> meshViews() {
        return components.enabled();
    }

    public Set<Material> materials() {
        return materials.keySet();
    }

    @Override
    protected void add(MeshView component) {
        if(component.enabled() && component.mesh() != null) {
            components.enabled().add(component);
            addMaterial(component.mesh().material());
        } else {
            components.disabled().add(component);
        }
    }

    @Override
    protected void remove(MeshView component) {
        super.remove(component);
        removeMaterial(component.mesh().material());
    }

    @Override
    protected void enable(MeshView component) {
        super.enable(component);
        addMaterial(component.mesh().material());
    }

    @Override
    protected void disable(MeshView component) {
        super.disable(component);
        removeMaterial(component.mesh().material());
    }

    @Override
    protected void removeAll() {
        super.removeAll();
        materials.clear();
    }

    private void addMaterial(Material material) {
        materials.compute(material, (mat, count) -> count == null ? 1 : ++count);
    }

    private void removeMaterial(Material material) {
        if(materials.get(material) == 0) {
            materials.remove(material);
        } else {
            materials.put(material, materials.get(material) - 1);
        }
    }

}
