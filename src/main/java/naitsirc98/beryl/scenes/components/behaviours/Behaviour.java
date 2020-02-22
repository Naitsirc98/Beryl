package naitsirc98.beryl.scenes.components.behaviours;

import naitsirc98.beryl.scenes.Component;

/**
 * A behaviour defines a logic for an Entity. All behaviour updates run in the same thread
 */
public class Behaviour extends Component<Behaviour> {

    private boolean started;

    @Override
    protected final void init() {
        super.init();
        started = false;
    }

    /**
     * Tells whether this behaviour has started or not
     *
     * @return true if {@link Behaviour#onStart()} has been called, false otherwise
     */
    public boolean started() {
        return started;
    }

    void start() {
        onStart();
        started = true;
    }

    /**
     * Gets called whenever this behaviour starts. This is executed the first time this behaviour gets updated
     */
    protected void onStart() {

    }

    /**
     * Called on the update pass of its scene.
     */
    protected void onUpdate() {

    }

    /**
     * Called after the update and the first process task pass of its scene. At this point, all deferred operations made in
     * {@link Behaviour#onUpdate()}, like destruction of scene objects, are completed.
     */
    protected void onLateUpdate() {

    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    protected void onDestroy() {

    }

    @Override
    public Class<? extends Component> type() {
        return Behaviour.class;
    }

    @Override
    protected final Behaviour self() {
        return this;
    }
}
