package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.meshes.Mesh;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface ModelLoader<T extends Mesh> {

    default Model<T> load(String path) {
        return load(Paths.get(path));
    }

    default Model<T> load(File file) {
        return load(file.toPath());
    }

    Model<T> load(Path path);

}
