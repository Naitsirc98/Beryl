package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.meshes.MeshView;

public interface MeshViewFactory<T> {

    MeshView create(T key, Material material);

}
