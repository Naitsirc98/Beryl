package naitsirc98.beryl.scenes;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.scenes.components.behaviours.Behaviour;
import naitsirc98.beryl.scenes.components.behaviours.BehaviourManager;

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
    private final BehaviourManager behaviours;
    private final Map<Class<? extends Component>, ComponentManager<?>> componentManagers;
    private final Queue<Runnable> taskQueue;

    public Scene() {
        entityManager = new EntityManager(this);
        // Create Component Managers
        behaviours = newInstance(BehaviourManager.class, this);
        componentManagers = createComponentManagersMap();
        // ---
        taskQueue = new ArrayDeque<>();
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
    }

    void render() {
        // TODO
    }

    void terminate() {
        // TODO
        processTasks();
        entityManager.destroy();
        componentManagers.values().forEach(ComponentManager::clear);
    }

    public void submit(Runnable task) {

        if(task == null) {
            Log.error("Cannot to submit a null task");
            return;
        }

        taskQueue.add(task);
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

    public int entityCount() {
        return entityManager.entityCount();
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

        entity.markDestroyed();

        destroyEntity(entity);
    }

    private void destroyEntity(Entity entity) {
        entityManager.destroy(entity);
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

        component.markDestroyed();

        submit(() -> destroyComponent(component));
    }

    void destroyNow(Component component) {

        if(component == null || component.destroyed()) {
            return;
        }

        component.markDestroyed();

        destroyComponent(component);
    }

    @SuppressWarnings("unchecked")
    private void destroyComponent(Component component) {
        managerOf(component.type()).remove(component);
        component.onDestroy();
    }

    @SuppressWarnings("unchecked")
    private <T extends Component> ComponentManager<T> managerOf(Class<T> type) {
        assertTrue(componentManagers.containsKey(type));
        return (ComponentManager<T>) componentManagers.get(type);
    }

    private Map<Class<? extends Component>, ComponentManager<?>> createComponentManagersMap() {

        Map<Class<? extends Component>, ComponentManager<?>> components = new HashMap<>();

        components.put(Behaviour.class, behaviours);

        return components;
    }
}
