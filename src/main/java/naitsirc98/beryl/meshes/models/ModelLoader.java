package naitsirc98.beryl.meshes.models;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface ModelLoader {

    default Model load(String path) {
        return load(Paths.get(path));
    }

    default Model load(File file) {
        return load(file.toPath());
    }

    Model load(Path path);

}
