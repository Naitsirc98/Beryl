package naitsirc98.beryl.scenes.components;

import naitsirc98.beryl.scenes.Component;
import naitsirc98.beryl.scenes.ComponentManager;
import naitsirc98.beryl.scenes.Scene;

public abstract class AbstractComponentManager<T extends Component> extends ComponentManager<T> {

    protected final ComponentContainer.Default<T> components;

    protected AbstractComponentManager(Scene scene) {
        super(scene);
        components = new ComponentContainer.Default<>();
    }

    @Override
    protected void add(T component) {
        components.add(component);
    }

    @Override
    protected void enable(T component) {
        components.enable(component);
    }

    @Override
    protected void disable(T component) {
        components.disable(component);
    }

    @Override
    protected void remove(T component) {
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
