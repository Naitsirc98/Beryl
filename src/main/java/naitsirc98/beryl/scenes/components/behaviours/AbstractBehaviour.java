package naitsirc98.beryl.scenes.components.behaviours;

import naitsirc98.beryl.scenes.Component;

import static naitsirc98.beryl.util.Asserts.assertTrue;

/**
 * A behaviour defines a logic for an Entity. All behaviour updates run in the same thread
 */
public abstract class AbstractBehaviour extends Component<AbstractBehaviour> {

    private boolean started;

    protected AbstractBehaviour() {

    }

    @Override
    protected void init() {
        super.init();
        assertTrue(this instanceof IUpdateBehaviour || this instanceof ILateBehaviour);
        started = false;
        onInit();
    }

    /**
     * Tells whether this behaviour has started or not
     *
     * @return true if {@link AbstractBehaviour#onStart()} has been called, false otherwise
     */
    public boolean started() {
        assertNotDeleted();
        return started;
    }

    void start() {
        onStart();
        started = true;
    }

    /**
     * Gets called whenever this behaviour is initialized.
     * */
    protected void onInit() {

    }

    /**
     * Gets called whenever this behaviour starts. This is executed the first time this behaviour is updated.
     */
    protected void onStart() {

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
    public final Class<? extends Component> type() {
        return AbstractBehaviour.class;
    }

    @Override
    protected final AbstractBehaviour self() {
        return this;
    }
}
