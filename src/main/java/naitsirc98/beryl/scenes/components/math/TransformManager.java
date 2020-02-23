package naitsirc98.beryl.scenes.components.math;

import naitsirc98.beryl.scenes.ComponentManager;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.ComponentContainer;

import java.util.stream.Stream;

public class TransformManager extends ComponentManager<Transform> {

    private final ComponentContainer.Default<Transform> components;

    private TransformManager(Scene scene) {
        super(scene);
        components = new ComponentContainer.Default<>();
    }

    public void update() {
        modifiedTransforms().forEach(Transform::update);
    }

    private Stream<Transform> modifiedTransforms() {
        return components.enabled().parallelStream().unordered().filter(Transform::modified);
    }

    @Override
    protected void add(Transform component) {
        components.add(component);
    }

    @Override
    protected void enable(Transform component) {
        components.enable(component);
    }

    @Override
    protected void disable(Transform component) {
        components.disable(component);
    }

    @Override
    protected void remove(Transform component) {
        components.remove(component);
    }

    @Override
    protected void removeAll() {
        components.clear();
    }

    @Override
    protected int size() {
        return components.size();
    }
}
