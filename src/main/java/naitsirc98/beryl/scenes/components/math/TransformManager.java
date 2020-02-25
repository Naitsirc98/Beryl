package naitsirc98.beryl.scenes.components.math;

import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.AbstractComponentManager;

import java.util.stream.Stream;

public final class TransformManager extends AbstractComponentManager<Transform> {

    private TransformManager(Scene scene) {
        super(scene);
    }

    public void update() {
        modifiedTransforms().forEach(Transform::update);
    }

    private Stream<Transform> modifiedTransforms() {
        return components.enabled().parallelStream().unordered().filter(Transform::modified);
    }
}
