package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.meshes.vertices.VertexLayout;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface ModelLoader {

    VertexLayout DEFAULT_VERTEX_LAYOUT = VertexLayout.VERTEX_LAYOUT_3D;

    default Model load(String path) {
        return load(Paths.get(path), DEFAULT_VERTEX_LAYOUT);
    }

    default Model load(String path, VertexLayout vertexLayout) {
        return load(Paths.get(path), vertexLayout);
    }

    default Model load(File file) {
        return load(file.toPath(), DEFAULT_VERTEX_LAYOUT);
    }

    default Model load(File file, VertexLayout vertexLayout) {
        return load(file.toPath(), vertexLayout);
    }

    default Model load(Path path) {
        return load(path, DEFAULT_VERTEX_LAYOUT);
    }

    Model load(Path path, VertexLayout vertexLayout);

}
