package naitsirc98.beryl.scenes.components.meshes;

import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.meshes.Mesh;

import java.util.List;
import java.util.Set;

public interface SceneMeshInfo {

    int modifications();

    List<Mesh> meshes();

    List<MeshView> instancesOf(Mesh mesh);

    List<MeshView> meshViews();

    Set<Material> materials();
}
