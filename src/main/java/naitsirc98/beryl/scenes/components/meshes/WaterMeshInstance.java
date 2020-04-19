package naitsirc98.beryl.scenes.components.meshes;

import naitsirc98.beryl.meshes.views.WaterMeshView;

public class WaterMeshInstance extends MeshInstance<WaterMeshView> {

    private boolean cameraIsUnderWater;

    @Override
    public Class<WaterMeshView> meshViewType() {
        return WaterMeshView.class;
    }

    public boolean cameraIsUnderWater() {
        return cameraIsUnderWater;
    }

    public WaterMeshInstance cameraIsUnderWater(boolean cameraIsUnderWater) {
        this.cameraIsUnderWater = cameraIsUnderWater;
        return this;
    }
}
