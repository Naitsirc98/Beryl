package naitsirc98.beryl.scenes;

import naitsirc98.beryl.core.Log;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public final class Scene {
    
    private final EntityManager entityManager;
    private final Map<Class<? extends Component>, ComponentManager<?>> componentManagers;
    private final Queue<Runnable> taskQueue;

    public Scene() {
        entityManager = new EntityManager(this);
        componentManagers = createComponentManagersMap();
        taskQueue = new ArrayDeque<>();
    }

    public synchronized void submit(Runnable task) {
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
        return entityManager.newEntity(name);
    }

    public void destroy(String entityName) {
        destroy(entityManager.get(entityName));
    }

    public void destroy(Entity entity) {
        if(entity == null || entity.destroyed()) {
            return;
        }
        entity.markDestroyed();
        submit(() -> entityManager.destroy(entity));
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> void destroy(T component) {
        if(component == null || component.destroyed()) {
            return;
        }
        managerOf(component.type()).destroy(component);
    }

    @SuppressWarnings("unchecked")
    private <T extends Component> ComponentManager<T> managerOf(Class<T> type) {
        return (ComponentManager<T>) componentManagers.get(type);
    }

    private Map<Class<? extends Component>, ComponentManager<?>> createComponentManagersMap() {
        return new HashMap<>();
    }

}
