package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.meshes.views.StaticMeshView;
import naitsirc98.beryl.util.collections.LookupTable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StaticModel {

    private final Path path;
    private final List<StaticMeshView> meshViews;
    private final LookupTable<String, StaticMeshView> meshNames;

    public StaticModel(Path path) {
        this.path = path;
        meshViews = new ArrayList<>();
        meshNames = new LookupTable<>();
    }

    public List<StaticMeshView> meshViews() {
        return meshViews;
    }

    public int numMeshViews() {
        return meshViews.size();
    }

    public StaticMeshView meshView(int index) {
        return meshViews.get(index);
    }

    public StaticMeshView meshView(String name) {
        return meshNames.valueOf(name);
    }

    void addMeshView(StaticMeshView meshView) {
        meshViews.add(meshView);
        meshNames.put(meshView.mesh().name(), meshView);
    }

    @Override
    public String toString() {
        return "StaticModel {" +
                "path=" + path +
                ", meshes=" + meshViews;
    }
}
