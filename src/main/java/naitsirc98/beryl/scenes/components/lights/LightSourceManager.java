package naitsirc98.beryl.scenes.components.lights;

import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.AbstractComponentManager;

import java.util.List;

public final class LightSourceManager extends AbstractComponentManager<LightSource> {

    private LightSourceManager(Scene scene) {
        super(scene);
    }

    public List<LightSource> lightSources() {
        return components.enabled();
    }

}
