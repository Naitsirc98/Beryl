package naitsirc98.beryl.scenes;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static naitsirc98.beryl.util.TypeUtils.getOrElse;
import static naitsirc98.beryl.util.TypeUtils.newInstanceUnsafe;

public final class Entity extends SceneObject {

    public static final int INVALID_INDEX = -1;

    public static final String UNNAMED = "__UNNAMED";


    private String name;
    private Scene scene;
    private Map<Class<? extends Component>, Component> components;
    private int index;
    private boolean enabled;

    Entity(String name, Scene scene, int index) {
        components = new HashMap<>();
        init(name, scene, index);
    }

    public <T extends Component> T add(Class<T> componentClass) {

        assertNonNull(componentClass);

        if(has(componentClass)) {
            throw new IllegalArgumentException("This entity has already a component of class " + componentClass);
        }

        T component = newInstanceUnsafe(componentClass);
        component.entity = this;

        components.put(componentClass, component);

        // TODO: add component to scene

        return component;
    }

    public <T> T get(Class<T> componentClass) {
        assertNonNull(componentClass);
        return componentClass.cast(components.get(componentClass));
    }

    public <T extends Component> T requires(Class<T> componentClass) {
        assertNonNull(componentClass);

        if(has(componentClass)) {
            return get(componentClass);
        }

        throw new NoSuchElementException("Component of class " + componentClass.getSimpleName() + " required but not present");
    }

    public void destroy(Class<? extends Component> componentClass) {
        // TODO:
    }

    public void destroy(Component component) {
        // TODO
    }

    public boolean has(Class<? extends Component> componentClass) {
        return components.containsKey(componentClass);
    }

    public boolean has(Component component) {
        return components.containsValue(component);
    }

    public int size() {
        return components.size();
    }

    public String name() {
        return name;
    }

    public Scene scene() {
        return scene;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public Entity enable() {
        if(!enabled) {
            enabled = true;
            // TODO: enable all components
        }
        return this;
    }

    @Override
    public Entity disable() {
        if(enabled) {
            enabled = false;
            // TODO: disable all components
        }
        return this;
    }

    @Override
    public void destroy() {
        scene.destroy(this);
    }

    @Override
    protected void onDestroy() {
        // TODO: destroy all components
        index = INVALID_INDEX;
        name = null;
        scene = null;
        components.clear();
        components = null;
    }

    int index() {
        return index;
    }

    void init(String name, Scene scene, int index) {
        this.name = getOrElse(name, UNNAMED);
        this.scene = assertNonNull(scene);
        this.index = index;
    }
}
