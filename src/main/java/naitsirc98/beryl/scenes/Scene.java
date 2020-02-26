package naitsirc98.beryl.scenes;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.scenes.components.behaviours.Behaviour;
import naitsirc98.beryl.scenes.components.behaviours.BehaviourManager;
import naitsirc98.beryl.scenes.components.camera.Camera;
import naitsirc98.beryl.scenes.components.camera.CameraManager;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.math.TransformManager;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Stream;

import static naitsirc98.beryl.scenes.Entity.UNTAGGED;
import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static naitsirc98.beryl.util.TypeUtils.newInstance;

public final class Scene {
    
    private final EntityManager entityManager;

    // === Component Managers
    private final CameraManager cameras;
    private final TransformManager transforms;
    private final BehaviourManager behaviours;

    private final Map<Class<? extends Component>, ComponentManager<?>> componentManagers;
    // ===

    private final Queue<Runnable> taskQueue;

    private boolean started;

    public Scene() {

        entityManager = new EntityManager(this);

        // === Component Managers
        cameras = newInstance(CameraManager.class, this);
        transforms = newInstance(TransformManager.class, this);
        behaviours = newInstance(BehaviourManager.class, this);

        componentManagers = createComponentManagersMap();
        // ===

        taskQueue = new ArrayDeque<>();
    }

    void start() {
        processTasks();
        started = true;
    }

    void update() {
        // TODO
        behaviours.update();
    }

    void processTasks() {
        while(!taskQueue.isEmpty()) {
            taskQueue.poll().run();
        }
    }

    void lateUpdate() {
        // TODO
        behaviours.lateUpdate();
        transforms.update();
        cameras.update();
    }

    void render() {
        // TODO
        Camera camera = camera();
        if(camera != null) {
            camera().renderingPath().render(camera);
        }
    }

    void terminate() {
        // TODO
        processTasks();
        entityManager.remove();
        componentManagers.values().forEach(ComponentManager::removeAll);
    }

    public boolean started() {
        return started;
    }

    public void submit(Runnable task) {

        if(task == null) {
            Log.error("Cannot to submit a null task");
            return;
        }

        taskQueue.add(task);
    }

    public Camera camera() {
        return cameras.mainCamera();
    }

    public void camera(Camera camera) {

        if(camera != null && camera.scene() != this) {
            Log.error("Cannot set a camera from another scene");
            return;
        }

        cameras.mainCamera(camera);
    }

    public Entity newEntity() {
        return entityManager.newEntity();
    }

    public Entity newEntity(String name) {
        return entityManager.newEntity(name, UNTAGGED);
    }

    public Entity newEntity(String name, String tag) {
        return entityManager.newEntity(name, tag);
    }

    public Entity entity(String name) {
        return entityManager.find(name);
    }

    public boolean exists(String name) {
        return entityManager.exists(name);
    }

    public int entityCount() {
        return entityManager.entityCount();
    }

    public int componentCount() {
        return componentManagers.values().stream().mapToInt(ComponentManager::size).sum();
    }

    public Stream<Entity> entities() {
        return entityManager.entities();
    }

    public Entity entityWithTag(String tag) {
        return entityManager.findWithTag(tag);
    }

    public Stream<Entity> entitiesWithTag(String tag) {
        return entityManager.findAllWithTags(tag);
    }

    public void destroy(String entityName) {
        destroy(entityManager.find(entityName));
    }

    public void destroy(Entity entity) {

        if(entity == null || entity.destroyed()) {
            return;
        }

        if(entity.scene() != this) {
            Log.error("Cannot destroy an Entity from another scene");
            return;
        }

        entity.markDestroyed();

        submit(() -> destroyEntity(entity));
    }

    public void destroyNow(String entityName) {
        destroyNow(entityManager.find(entityName));
    }

    public void destroyNow(Entity entity) {

        if(entity == null || entity.destroyed()) {
            return;
        }

        if(entity.scene() != this) {
            Log.error("Cannot destroy an Entity from another scene");
            return;
        }

        destroyEntity(entity);
    }

    private void destroyEntity(Entity entity) {
        entityManager.remove(entity);
        entity.delete();
    }

    @SuppressWarnings("unchecked")
    <T extends Component> void add(T component) {
        assertNonNull(component);
        ComponentManager<T> manager = managerOf(component.type());
        manager.add(component);
        component.manager = manager;
    }

    void destroy(Component component) {

        if(component == null || component.destroyed()) {
            return;
        }

        if(component.scene() != this) {
            Log.error("Cannot destroy a Component from another scene");
            return;
        }

        component.markDestroyed();

        submit(() -> destroyComponent(component));
    }

    void destroyNow(Component component) {

        if(component == null || component.destroyed()) {
            return;
        }

        if(component.scene() != this) {
            Log.error("Cannot destroy a Component from another scene");
            return;
        }

        destroyComponent(component);
    }

    @SuppressWarnings("unchecked")
    void destroyComponent(Component component) {
        managerOf(component.type()).remove(component);
        component.delete();
    }

    @SuppressWarnings("unchecked")
    private <T extends Component> ComponentManager<T> managerOf(Class<T> type) {
        assertTrue(componentManagers.containsKey(type));
        return (ComponentManager<T>) componentManagers.get(type);
    }

    private Map<Class<? extends Component>, ComponentManager<?>> createComponentManagersMap() {

        Map<Class<? extends Component>, ComponentManager<?>> components = new HashMap<>();

        components.put(Behaviour.class, behaviours);
        components.put(Transform.class, transforms);
        components.put(Camera.class, cameras);

        return components;
    }
}
