package naitsirc98.beryl.scenes.components.meshes;

import naitsirc98.beryl.materials.Material;

import java.util.List;
import java.util.Set;

public interface SceneMeshInfo {

    int modifications();

    List<MeshView> meshViews();

    Set<Material> materials();
}
