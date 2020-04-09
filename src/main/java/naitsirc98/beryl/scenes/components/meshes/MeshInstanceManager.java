package naitsirc98.beryl.scenes.components.meshes;

import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.meshes.MeshView;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.AbstractComponentManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class MeshInstanceManager extends AbstractComponentManager<MeshInstance> implements SceneMeshInfo {

    private final List<MeshView> meshViews;
    private final Map<MeshView, List<MeshInstance>> instancesTable;
    private List<Material> materials;
    private final AtomicInteger modifications;
    private int lastModifications;

    protected MeshInstanceManager(Scene scene) {
        super(scene);
        meshViews = new ArrayList<>();
        instancesTable = new HashMap<>();
        materials = new ArrayList<>();
        modifications = new AtomicInteger();
        lastModifications = Integer.MIN_VALUE;
    }

    public void update() {

        final int modifications = modifications();

        if(lastModifications != modifications) {
            materials = meshViews.parallelStream().map(MeshView::material).distinct().collect(Collectors.toList());
            lastModifications = modifications();
        }
    }

    @Override
    protected void add(MeshInstance instance) {
        super.add(instance);

        if(instance.enabled()) {
            addInstance(instance);
        }
    }

    @Override
    protected void enable(MeshInstance instance) {
        super.enable(instance);
        // addInstance(instance);
    }

    @Override
    protected void disable(MeshInstance instance) {
        super.disable(instance);
        // removeInstance(instance);
    }

    @Override
    protected void remove(MeshInstance instance) {
        super.remove(instance);
        removeInstance(instance);
    }

    @Override
    protected void removeAll() {
        super.removeAll();
        meshViews.clear();
        instancesTable.clear();
        materials.clear();
    }

    @Override
    public int modifications() {
        return modifications.get();
    }

    @Override
    public List<MeshView> meshViews() {
        return meshViews;
    }

    @Override
    public List<MeshInstance> instances() {
        return components.enabled();
    }

    @Override
    public Map<MeshView, List<MeshInstance>> instancesTable() {
        return instancesTable;
    }

    @Override
    public List<Material> materials() {
        return materials;
    }

    private void addInstance(MeshInstance instance) {

        for(MeshView meshView : instance) {

            List<MeshInstance> instances;

            if(!instancesTable.containsKey(meshView)) {
                meshViews.add(meshView);
                instances = new ArrayList<>(1);
                instancesTable.put(meshView, instances);
            } else {
                instances = instancesTable.get(meshView);
            }

            instances.add(instance);

            modifications.incrementAndGet();
        }
    }

    private void removeInstance(MeshInstance instance) {

        for(MeshView meshView : instance) {

            List<MeshInstance> instances = instancesTable.get(meshView);

            instances.remove(instance);

            if(instances.isEmpty()) {
                instancesTable.remove(meshView);
                meshViews.remove(meshView);
            }

            modifications.incrementAndGet();

        }
    }
}
