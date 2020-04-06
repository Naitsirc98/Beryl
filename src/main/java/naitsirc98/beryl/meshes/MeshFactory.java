package naitsirc98.beryl.meshes;

import naitsirc98.beryl.materials.Material;

@FunctionalInterface
public interface MeshFactory<K> {

    Mesh create(K key, Material material);

}