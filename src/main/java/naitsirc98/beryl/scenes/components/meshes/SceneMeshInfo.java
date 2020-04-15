package naitsirc98.beryl.scenes.components.meshes;

import naitsirc98.beryl.meshes.views.MeshView;

import java.util.Map;

public interface SceneMeshInfo {

    Map<Class<MeshView>, MeshInstanceList> allInstances();

    <T extends MeshView, U extends MeshInstance> MeshInstanceList<U> meshViewsOfType(Class<T> meshViewType);
}
