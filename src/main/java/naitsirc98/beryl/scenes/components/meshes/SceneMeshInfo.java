package naitsirc98.beryl.scenes.components.meshes;

import naitsirc98.beryl.meshes.views.MeshView;

import java.util.Map;

public interface SceneMeshInfo {

    Map<Class<? extends MeshView>, MeshInstanceList> allInstances();

    MeshInstanceList<StaticMeshInstance> getStaticMeshInstances();

    MeshInstanceList<WaterMeshInstance> getWaterMeshInstances();
}
