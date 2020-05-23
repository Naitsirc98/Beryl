package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.util.collections.LookupTable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class StaticModel implements Iterable<StaticMesh> {

    private final Path path;
    private final List<StaticMesh> meshes;
    private final LookupTable<String, StaticMesh> meshNames;

    public StaticModel(Path path) {
        this.path = path;
        meshes = new ArrayList<>();
        meshNames = new LookupTable<>();
    }

    public List<StaticMesh> meshes() {
        return Collections.unmodifiableList(meshes);
    }

    public int numMeshes() {
        return meshes.size();
    }

    public StaticMesh mesh(int index) {
        return meshes.get(index);
    }

    public StaticMesh mesh(String name) {
        return meshNames.valueOf(name);
    }

    void addMesh(StaticMesh mesh) {
        meshes.add(mesh);
        meshNames.put(mesh.name(), mesh);
    }

    @Override
    public String toString() {
        return "StaticModel {" +
                "path=" + path +
                ", meshes=" + meshes;
    }

    @Override
    public Iterator<StaticMesh> iterator() {
        return meshes.iterator();
    }
}
