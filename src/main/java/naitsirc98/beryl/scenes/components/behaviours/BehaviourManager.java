package naitsirc98.beryl.scenes.components.behaviours;

import naitsirc98.beryl.scenes.ComponentManager;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.util.collections.FastIterableSet;

/**
 * The Behaviour Manager updates the {@link AbstractBehaviour} instances of a scene
 */
public class BehaviourManager extends ComponentManager<AbstractBehaviour> {

    private final FastIterableSet<IUpdateBehaviour> updateBehaviours;
    private final FastIterableSet<ILateBehaviour> lateBehaviours;
    private int size;

    private BehaviourManager(Scene scene) {
        super(scene);
        updateBehaviours = new FastIterableSet<>();
        lateBehaviours = new FastIterableSet<>();
    }

    /**
     * Performs the update pass
     */
    public void update() {
         updateBehaviours.parallelStream().unordered().forEach(IUpdateBehaviour::onUpdate);
    }

    /**
     * Performs the late-update pass
     */
    public void lateUpdate() {
        for(ILateBehaviour behaviour : lateBehaviours) {
            behaviour.onLateUpdate();
        }
    }

    @Override
    protected void add(AbstractBehaviour component) {

        if(component.enabled()) {

            addToProperCollection(component);

            component.onStart();

        }

        ++size;
    }

    @Override
    protected void enable(AbstractBehaviour component) {

        addToProperCollection(component);

        if(!component.started()) {
            component.start();
        }
    }

    @Override
    protected void disable(AbstractBehaviour component) {
        removeFromProperCollection(component);
    }

    @Override
    protected void remove(AbstractBehaviour component) {
        if(component.enabled()) {
            removeFromProperCollection(component);
        }
        --size;
    }

    @Override
    protected void removeAll() {
        updateBehaviours.clear();
        lateBehaviours.clear();
        size = 0;
    }

    @Override
    protected int size() {
        return size;
    }

    private void addToProperCollection(AbstractBehaviour component) {

        if(component instanceof IUpdateBehaviour) {
            updateBehaviours.add((IUpdateBehaviour) component);
        }

        if(component instanceof ILateBehaviour) {
            lateBehaviours.add((ILateBehaviour) component);
        }
    }

    private void removeFromProperCollection(AbstractBehaviour component) {

        if(component instanceof IUpdateBehaviour) {
            updateBehaviours.remove(component);
        }

        if(component instanceof ILateBehaviour) {
            lateBehaviours.remove(component);
        }
    }
}
