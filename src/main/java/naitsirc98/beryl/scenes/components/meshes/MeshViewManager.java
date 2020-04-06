package naitsirc98.beryl.scenes.components.meshes;

import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.AbstractComponentManager;

import java.util.*;

public final class MeshViewManager extends AbstractComponentManager<MeshView> implements SceneMeshInfo {


    private final List<Mesh> meshes;
    private final Map<Mesh, List<MeshView>> meshInstances;
    private final Map<Material, Integer> materials;
    private volatile int modifications;

    private MeshViewManager(Scene scene) {
        super(scene);
        meshes = new ArrayList<>();
        meshInstances = new HashMap<>();
        materials = new HashMap<>();
    }

    @Override
    public int modifications() {
        return modifications;
    }

    @Override
    public List<Mesh> meshes() {
        return meshes;
    }

    @Override
    public List<MeshView> instancesOf(Mesh mesh) {
        return meshInstances.get(mesh);
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
            addAllMeshes(component);
            addAllMaterials(component);
        } else {
            components.disabled().add(component);
        }
    }

    @Override
    protected void remove(MeshView component) {
        super.remove(component);
        component.forEach(mesh -> removeMeshInstance(mesh, component));
        component.materials().forEach(this::removeMaterial);
    }

    @Override
    protected void enable(MeshView component) {
        super.enable(component);
        component.forEach(mesh -> addMeshInstance(mesh, component));
        component.materials().forEach(this::addMaterial);
    }

    @Override
    protected void disable(MeshView component) {
        super.disable(component);
        component.forEach(mesh -> removeMeshInstance(mesh, component));
        component.materials().forEach(this::removeMaterial);
    }

    @Override
    protected void removeAll() {
        super.removeAll();
        meshes.clear();
        meshInstances.clear();
        materials.clear();
    }

    private boolean containsMesh(Mesh mesh) {
        return meshInstances.containsKey(mesh);
    }

    private void addAllMeshes(MeshView meshView) {
        meshView.forEach(mesh -> addMeshInstance(mesh, meshView));
    }

    void addMeshInstance(Mesh mesh, MeshView meshView) {

        List<MeshView> instances;

        if(containsMesh(mesh)) {
            instances = meshInstances.get(mesh);
        } else {
            instances = new ArrayList<>(1);
            meshInstances.put(mesh, instances);
            meshes.add(mesh);
        }

        instances.add(meshView);
    }

    void removeMeshInstance(Mesh mesh, MeshView meshView) {

        List<MeshView> instances = meshInstances.get(mesh);

        instances.remove(meshView);

        if(instances.isEmpty()) {
            meshes.remove(mesh);
            meshInstances.remove(mesh);
        }
    }

    private void addAllMaterials(MeshView meshView) {
        meshView.materials().forEach(this::addMaterial);
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
