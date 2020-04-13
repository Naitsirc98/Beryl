package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.meshes.Mesh;

public final class ModelMesh<T extends Mesh> {

    private final Model<T> model;
    private final int index;
    private final T mesh;

    ModelMesh(Model<T> model, int index, T mesh) {
        this.model = model;
        this.index = index;
        this.mesh = mesh;
    }

    public int index() {
        return index;
    }

    public String name() {
        return model.nameOf(this);
    }

    public T mesh() {
        return mesh;
    }
}
