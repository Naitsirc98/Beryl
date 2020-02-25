package naitsirc98.beryl.scenes.components.behaviours;

import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.AbstractComponentManager;
import naitsirc98.beryl.scenes.components.ComponentContainer;

/**
 * The Behaviour Manager updates the {@link Behaviour} instances of a scene
 */
public class BehaviourManager extends AbstractComponentManager<Behaviour> {

    private BehaviourManager(Scene scene) {
        super(scene);
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
}
