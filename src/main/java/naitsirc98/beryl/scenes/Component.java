package naitsirc98.beryl.scenes;

/**
 * A Component defines a single property or behaviour of an Entity. They are created and destroyed by its entity.
 *
 * Components are managed by {@link ComponentManager} instances.
 *
 * Components have a class and a type:
 *
 *  - The class of a component is its exact class, this is, what {@link Object#getClass()}
 *    returns. This is to uniquely differentiate between two {@link Component}.
 *
 *  - The type of a component refers to what group of components it belongs to. This could be thought as the superclass
 *    of the component.
 *
 * With the two concepts mentioned above, we can differentiate 2 {@link Component} of different classes, but treat components
 * of the same type with the same {@link ComponentManager}.
 *
 * @param <SELF> its own class.
 */
public abstract class Component<SELF extends Component> extends SceneObject {

    Entity entity;
    ComponentManager<SELF> manager;
    private boolean enabled;


    @Override
    protected void init() {
        super.init();
        enabled = true;
    }

    /**
     * Returns the entity of this component
     *
     * @return the entity
     */
    public Entity entity() {
        assertNotDeleted();
        return entity;
    }

    /**
     * Returns the name of this component, which is the same as its entity
     *
     * @return this component's name
     * */
    public String name() {
        assertNotDeleted();
        return entity.name();
    }

    /**
     * Returns the tag of this component, which is the same as its entity
     *
     * @return this component's tag
     * */
    public String tag() {
        assertNotDeleted();
        return entity.tag();
    }

    /**
     * Tells whether this component is already added to the scene or not
     *
     * @return true if this component is already managed by its scene, false otherwise
     * */
    public final boolean active() {
        assertNotDeleted();
        return manager != null;
    }

    /**
     * Returns a component of the given class contained by its entity
     *
     * @param componentClass the component class
     * @return the component or null if it does not exists
     */
    public final <T extends Component> T get(Class<T> componentClass) {
        assertNotDeleted();
        return entity.get(componentClass);
    }

    /**
     * Indicates that requires a component of the given class to be present in its entity
     *
     * @param componentClass the component class
     * @return the component
     */
    public final <T extends Component> T requires(Class<T> componentClass) {
        assertNotDeleted();
        return entity.requires(componentClass);
    }

    protected ComponentManager<SELF> manager() {
        assertNotDeleted();
        return manager;
    }

    @Override
    public Scene scene() {
        assertNotDeleted();
        return entity().scene();
    }

    @Override
    public boolean enabled() {
        assertNotDeleted();
        return entity.enabled() && enabled;
    }

    @Override
    public final SELF enable() {
        assertNotDeleted();
        if(!enabled() && active()) {
            doLater(() -> {
                manager.enable(self());
                onEnable();
            });
        }
        return self();
    }

    @Override
    public final SELF disable() {
        assertNotDeleted();
        if(enabled() && active()) {
            doLater(() -> {
                manager.disable(self());
                onDisable();
            });
        }
        return self();
    }

    /**
     * Returns the type of this component. Types are used to group similar components together to be used by the same
     * {@link ComponentManager}
     *
     * @return the type
     */
    public abstract Class<? extends Component> type();

    @Override
    public boolean destroyed() {
        return super.destroyed() || entity.destroyed();
    }

    @Override
    public final void destroy() {
        assertNotDeleted();
        if(active()) {
            entity.destroy(this);
        }
    }

    @Override
    public void destroyNow() {
        assertNotDeleted();
        if(active()) {
            entity.destroyNow(this);
        }
    }

    /**
     * Runs whenever this component becomes enabled
     */
    protected abstract void onEnable();

    /**
     * Runs whenever this component becomes disabled
     */
    protected abstract void onDisable();

    /**
     * Returns this component. This is to avoid expensive generic casts.
     *
     * @return this component
     */
    protected abstract SELF self();
}
