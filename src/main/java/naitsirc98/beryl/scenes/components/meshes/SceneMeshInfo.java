package naitsirc98.beryl.scenes.components.meshes;

import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.meshes.MeshView;

import java.util.List;
import java.util.Map;

public interface SceneMeshInfo {

    int modifications();

    List<MeshView> meshViews();

    List<MeshInstance> instances();

    Map<MeshView, List<MeshInstance>> instancesTable();

    List<Material> materials();
}
