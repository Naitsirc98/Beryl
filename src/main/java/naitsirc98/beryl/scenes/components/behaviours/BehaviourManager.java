package naitsirc98.beryl.scenes.components.behaviours;

import naitsirc98.beryl.scenes.ComponentManager;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.ComponentContainer;

/**
 * The Behaviour Manager updates the {@link Behaviour} instances of a scene
 */
public class BehaviourManager extends ComponentManager<Behaviour> {

    private final ComponentContainer.Default<Behaviour> components;

    private BehaviourManager(Scene scene) {
        super(scene);
        components = new ComponentContainer.Default<>();
    }

    /**
     * Performs the update pass
     */
    public void update() {
        for(Behaviour behaviour : components.enabled()) {
            behaviour.onUpdate();
        }
    }

    /**
     * Performs the late-update pass
     */
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
    protected void remove(Behaviour component) {
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
