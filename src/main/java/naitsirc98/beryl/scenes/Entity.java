package naitsirc98.beryl.scenes;

import naitsirc98.beryl.logging.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static naitsirc98.beryl.util.types.TypeUtils.getOrElse;
import static naitsirc98.beryl.util.types.TypeUtils.newInstanceUnsafe;

/**
 * An Entity is a container for {@link Component}. An entity can only have 1 component of 1 component class, but it may contain
 * many components of the same type. Refer to {@link Component} to see the difference between class and type in this context.
 */
public final class Entity extends SceneObject implements Iterable<Component> {

    public static final int INVALID_INDEX = -1;
    public static final String UNNAMED = "__UNNAMED";
    public static final String UNTAGGED = "__UNTAGGED";

    /**
     * Creates an entity in the currently active scene, or in the front scene if there is no active scene.
     *
     * @return the entity
     */
    public static Entity newEntity() {
        if(noAvailableScene()) {
            return null;
        }
        return SceneManager.scene().newEntity();
    }

    /**
     * Creates an entity with the given name in the currently active scene,
     * or in the front scene if there is no active scene.
     *
     * @param name the name
     * @return the entity
     */
    public static Entity newEntity(String name) {
        if(noAvailableScene()) {
            return null;
        }
        return SceneManager.scene().newEntity(name);
    }

    /**
     * Creates an entity with the given name and tag in the currently active scene,
     * or in the front scene if there is no active scene.
     *
     * @param name the name
     * @param tag  the tag
     * @return the entity
     */
    public static Entity newEntity(String name, String tag) {
        if(noAvailableScene()) {
            return null;
        }
        return SceneManager.scene().newEntity(name, tag);
    }

    private static boolean noAvailableScene() {
        if(SceneManager.scene() == null) {
            Log.fatal("There is no scene available to create the Entity");
            return true;
        }
        return false;
    }

    private String name;
    private String tag;
    private Scene scene;
    private Map<Class<? extends Component>, Component> components;
    private int index;
    private boolean enabled;

    /**
     * Instantiates a new Entity.
     *
     * @param name  the name
     * @param tag   the tag
     * @param scene the scene
     * @param index the index
     */
    Entity(String name, String tag, Scene scene, int index) {
        components = new HashMap<>();
        init(name, tag, scene, index);
    }

    /**
     * Adds a new component of the given class to this entity.
     *
     * @param componentClass the component class
     * @return the added component, or the previous component of the given class if it was already added.
     */
    public synchronized <T extends Component> T add(Class<T> componentClass) {
        assertNotDeleted();
        assertNonNull(componentClass);

        if(has(componentClass)) {
            return getComponent(componentClass);
        }

        T component = newInstanceUnsafe(componentClass);
        component.init();
        component.entity = this;

        components.put(componentClass, component);

        if(!destroyed()) {
            doLater(() -> scene.add(component));
        }

        return component;
    }

    /**
     * Returns a component of the specified class. It will be created if no component of the given class already exists.
     *
     * @param componentClass the component class
     * @return the component
     */
    public <T extends Component> T get(Class<T> componentClass) {
        assertNotDeleted();
        assertNonNull(componentClass);

        if(has(componentClass)) {
            return getComponent(componentClass);
        }

        return add(componentClass);
    }

    private <T extends Component> T getComponent(Class<T> componentClass) {
        assertNotDeleted();
        return componentClass.cast(components.get(componentClass));
    }

    /**
     * Indicates that the caller requires a component of the given class to exists.
     *
     * @param componentClass the component class
     * @return the component
     * @throws NoSuchElementException if there is no component of the specified class in this entity
     */
    public <T extends Component> T requires(Class<T> componentClass) {
        assertNotDeleted();
        assertNonNull(componentClass);

        if(has(componentClass)) {
            return get(componentClass);
        }

        throw new NoSuchElementException("Component of class " + componentClass.getSimpleName() + " required but not present");
    }

    /**
     * Destroys the component of the given class, if exists
     *
     * @param componentClass the component class
     */
    public void destroy(Class<? extends Component> componentClass) {
        assertNotDeleted();
        destroy(get(componentClass));
    }

    /**
     * Destroys the given component if it is contained in this entity
     *
     * @param component the component
     */
    public void destroy(Component component) {
        assertNotDeleted();

        if(component == null || component.destroyed() || component.entity() != this) {
            return;
        }

        scene.destroy(component);
        doLater(() -> components.remove(component.getClass()));
    }

    /**
     * Destroys the given component immediately if it is contained in this entity. This is highly discouraged, call
     * {@link Entity#destroy(Component)} instead
     *
     * @param component the component
     */
    public void destroyNow(Component component) {
        assertNotDeleted();

        if(component == null || component.destroyed() || component.entity() != this) {
            return;
        }

        scene.destroyNow(component);
        components.remove(component.getClass());
    }

    /**
     * Tells whether this entity contains a component of the given class or not
     *
     * @param componentClass the component class
     * @return true if it contains a component of the specified class, false otherwise
     */
    public boolean has(Class<? extends Component> componentClass) {
        assertNotDeleted();
        return components.containsKey(componentClass);
    }

    /**
     * Tells whether this entity contains the specified component or not
     *
     * @param component the component
     * @return true if it contains the component, false otherwise
     */
    public boolean has(Component component) {
        assertNotDeleted();
        return components.containsValue(component);
    }

    /**
     * Returns how many components has this entity
     *
     * @return the component count
     */
    public int componentCount() {
        assertNotDeleted();
        return components.size();
    }

    /**
     * Returns a stream with all the components of this entity
     *
     * @return all the components of this entity
     */
    public Stream<Component> components() {
        assertNotDeleted();
        return components.values().stream();
    }

    /**
     * Returns a stream with the components of the specified type in this entity
     *
     * @param type the component type
     * @return the entities of the given type in this entity
     */
    public Stream<Component> components(Class<? extends Component> type) {
        assertNotDeleted();
        return this.components.values().stream().filter(c -> c.type().equals(type));
    }

    /**
     * Returns the name of this entity. Names are unique within a scene
     *
     * @return the name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the tag of this entity
     *
     * @return the tag
     */
    public String tag() {
        return tag;
    }

    @Override
    public Scene scene() {
        return scene;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public Entity enable() {
        assertNotDeleted();
        if(!enabled) {
            components.values().forEach(Component::enable);
            enabled = true;
        }
        return this;
    }

    @Override
    public Entity disable() {
        assertNotDeleted();
        if(enabled) {
            components.values().forEach(Component::disable);
            enabled = false;
        }
        return this;
    }

    @Override
    public void destroy() {
        assertNotDeleted();
        scene.destroy(this);
    }

    @Override
    public void destroyNow() {
        assertNotDeleted();
        scene.destroyNow(this);
    }

    @Override
    public Iterator<Component> iterator() {
        assertNotDeleted();
        return components.values().iterator();
    }

    @Override
    protected void onDestroy() {
        components.values().forEach(scene::destroyComponent);
        index = INVALID_INDEX;
        name = null;
        tag = null;
        scene = null;
        components.clear();
        components = null;
    }

    /**
     * Index of this entity in its EntityManager
     *
     * @return the index
     */
    int index() {
        return index;
    }

    /**
     * Init.
     *
     * @param name  the name
     * @param tag   the tag
     * @param scene the scene
     * @param index the index
     */
    void init(String name, String tag, Scene scene, int index) {
        this.name = getOrElse(name, UNNAMED);
        this.tag = getOrElse(tag, UNTAGGED);
        this.scene = assertNonNull(scene);
        this.index = index;
        enabled = true;
    }

}
