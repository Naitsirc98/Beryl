package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.util.collections.LookupTable;

import java.nio.file.Path;
import java.util.Arrays;

public class StaticModel {

    private final Path path;
    private final StaticMesh[] meshes;
    private final LookupTable<String, StaticMesh> meshNames;

    public StaticModel(Path path, int numMeshes) {
        this.path = path;
        meshes = new StaticMesh[numMeshes];
        meshNames = new LookupTable<>();
    }

    public StaticMesh[] meshes() {
        return meshes;
    }

    public int numMeshes() {
        return meshes.length;
    }

    public StaticMesh mesh(int index) {
        return meshes[index];
    }

    public StaticMesh mesh(String name) {
        return meshNames.valueOf(name);
    }

    public boolean released() {
        return Arrays.stream(meshes).anyMatch(Mesh::released);
    }

    @Override
    public String toString() {
        return "StaticModel{" +
                "path=" + path +
                ", meshes=" + Arrays.stream(meshes).map(Mesh::name).reduce((s1, s2) -> s1 + ", " + s2).get();
    }
}
