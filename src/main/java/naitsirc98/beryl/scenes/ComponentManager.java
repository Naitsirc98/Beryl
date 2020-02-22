package naitsirc98.beryl.scenes;

/**
 * A Component Manager is in charge of updating Components of a certain type properly.
 *
 * @param <T> the type parameter
 */
public abstract class ComponentManager<T extends Component> {


    private final Scene scene;

    /**
     * Instantiates a new Component Manager.
     *
     * @param scene the scene
     */
    protected ComponentManager(Scene scene) {
        this.scene = scene;
    }

    /**
     * Returns the scene of this Component Manager
     *
     * @return the scene
     */
    public final Scene scene() {
        return scene;
    }

    /**
     * Adds the specified component to this Component Manager
     *
     * @param component the component
     * */
    protected abstract void add(T component);

    /**
     * Indicates that the given component is enabled.
     *
     * @param component the component
     */
    protected abstract void enable(T component);

    /**
     * Indicates that the given component is disabled.
     *
     * @param component the component
     */
    protected abstract void disable(T component);

    /**
     * Remove the component from this Component Manager
     *
     * @param component the component
     */
    protected abstract void remove(T component);

    /**
     * Removes all components
     */
    protected abstract void removeAll();

    /**
     * Returns the number of components managed by this Component Manager
     *
     * @return the number of components
     * */
    protected abstract int size();
}
