package naitsirc98.beryl.scenes.components.meshes;

import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.AbstractComponentManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MeshViewManager extends AbstractComponentManager<MeshView> implements SceneMeshInfo {

    private final Map<Material, Integer> materials;
    private volatile int modifications;

    private MeshViewManager(Scene scene) {
        super(scene);
        materials = new HashMap<>();
    }

    @Override
    public int modifications() {
        return modifications;
    }

    @Override
    public List<MeshView> meshViews() {
        return components.enabled();
    }

    @Override
    public Set<Material> materials() {
        return materials.keySet();
    }

    @Override
    protected void add(MeshView component) {
        if(component.enabled() && component.mesh() != null) {
            components.enabled().add(component);
            component.materials().forEach(this::addMaterial);
        } else {
            components.disabled().add(component);
        }
    }

    @Override
    protected void remove(MeshView component) {
        super.remove(component);
        component.materials().forEach(this::removeMaterial);
    }

    @Override
    protected void enable(MeshView component) {
        super.enable(component);
        component.materials().forEach(this::addMaterial);
    }

    @Override
    protected void disable(MeshView component) {
        super.disable(component);
        component.materials().forEach(this::removeMaterial);
    }

    @Override
    protected void removeAll() {
        super.removeAll();
        materials.clear();
    }

    void addMaterial(Material material) {
        materials.compute(material, (mat, count) -> count == null ? 1 : ++count);
    }

    void removeMaterial(Material material) {
        if(!materials.containsKey(material)) {
            return;
        }
        if(materials.get(material) == 0) {
            materials.remove(material);
        } else {
            materials.put(material, materials.get(material) - 1);
        }
    }

    void markModified() {
        ++modifications;
    }
}
