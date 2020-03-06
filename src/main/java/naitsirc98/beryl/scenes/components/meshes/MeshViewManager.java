package naitsirc98.beryl.scenes.components.meshes;

import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.AbstractComponentManager;

import java.util.List;

public final class MeshViewManager extends AbstractComponentManager<MeshView> {

    private MeshViewManager(Scene scene) {
        super(scene);
    }

    public List<MeshView> meshViews() {
        return components.enabled();
    }

    @Override
    protected void add(MeshView component) {
        if(component.enabled() && component.mesh() != null) {
            components.enabled().add(component);
        } else {
            components.disabled().add(component);
        }
    }
}
