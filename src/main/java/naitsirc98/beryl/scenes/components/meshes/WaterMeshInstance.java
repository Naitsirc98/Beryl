package naitsirc98.beryl.scenes.components.meshes;

import naitsirc98.beryl.meshes.views.WaterMeshView;

public class WaterMeshInstance extends MeshInstance<WaterMeshView> {

    @Override
    public Class<WaterMeshView> meshViewType() {
        return WaterMeshView.class;
    }
}
