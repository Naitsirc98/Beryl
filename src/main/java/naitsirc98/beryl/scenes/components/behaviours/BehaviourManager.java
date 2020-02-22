package naitsirc98.beryl.scenes.components.behaviours;

import naitsirc98.beryl.scenes.ComponentManager;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.ComponentContainer;

public class BehaviourManager extends ComponentManager<Behaviour> {

    private final ComponentContainer.Default<Behaviour> components;

    private BehaviourManager(Scene scene) {
        super(scene);
        components = new ComponentContainer.Default<>();
    }

    public void update() {
        for(Behaviour behaviour : components.enabled()) {
            behaviour.onUpdate();
        }
    }

    public void lateUpdate() {
        for(Behaviour behaviour : components.enabled()) {
            behaviour.onLateUpdate();
        }
    }

    @Override
    protected void add(Behaviour component) {
        components.add(component);
        if(component.enabled()) {
            component.onStart();
        }
    }

    @Override
    protected void enable(Behaviour component) {
        components.enable(component);
        if(!component.started()) {
            component.start();
        }
    }

    @Override
    protected void disable(Behaviour component) {
        components.disable(component);
    }

    @Override
    protected void destroy(Behaviour component) {
        components.remove(component);
    }

    @Override
    protected void clear() {
        components.clear();
    }
}
