package naitsirc98.beryl.scenes.components.camera;

import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.AbstractComponentManager;

public final class CameraManager extends AbstractComponentManager<Camera> {

    private Camera mainCamera;

    protected CameraManager(Scene scene) {
        super(scene);
    }

    public Camera mainCamera() {
        return mainCamera;
    }

    public void mainCamera(Camera camera) {
        this.mainCamera = camera;
    }

    public void update() {
        components.enabled().parallelStream().unordered().filter(Camera::modified).forEach(Camera::update);
    }

    @Override
    protected void add(Camera component) {
        if(size() == 0) {
            mainCamera = component;
        }
        super.add(component);
    }

    @Override
    protected void remove(Camera component) {
        if(component == mainCamera) {
            mainCamera = null;
        }
        super.remove(component);
    }

    @Override
    protected void removeAll() {
        mainCamera = null;
        super.removeAll();
    }
}
