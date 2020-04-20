package naitsirc98.beryl.scenes.components.animations;

import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.AbstractComponentManager;

public class AnimatorManager extends AbstractComponentManager<Animator> {

    protected AnimatorManager(Scene scene) {
        super(scene);
    }

    public void update() {
        components.enabled().parallelStream().forEach(Animator::update);
    }

}
