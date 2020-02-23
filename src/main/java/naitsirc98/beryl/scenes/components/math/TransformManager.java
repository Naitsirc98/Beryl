package naitsirc98.beryl.scenes.components.math;

import naitsirc98.beryl.scenes.ComponentManager;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.ComponentContainer;

import java.util.*;
import java.util.stream.Stream;

public class TransformManager extends ComponentManager<Transform> {

    private final ComponentContainer<Transform, HashSet<Transform>, HashSet<Transform>> components;
    private final Queue<Transform> modifiedTransforms;

    private TransformManager(Scene scene) {
        super(scene);
        components = new ComponentContainer<>(new HashSet<>(), new HashSet<>());
        modifiedTransforms = new ArrayDeque<>();
    }

    public void update() {
        while(!modifiedTransforms.isEmpty()) {
            modifiedTransforms.poll().update();
        }
    }

    @Override
    protected void add(Transform component) {
        components.add(component);
        if(component.modified()) {
            markModified(component);
        }
    }

    @Override
    protected void enable(Transform component) {
        components.enable(component);
    }

    @Override
    protected void disable(Transform component) {
        components.disable(component);
        modifiedTransforms.remove(component);
    }

    @Override
    protected void remove(Transform component) {
        components.remove(component);
        modifiedTransforms.remove(component);
    }

    @Override
    protected void removeAll() {
        components.clear();
    }

    @Override
    protected int size() {
        return components.size();
    }

    void markModified(Transform transform) {
        modifiedTransforms.add(transform);
    }
}
