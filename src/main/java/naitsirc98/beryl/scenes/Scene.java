package naitsirc98.beryl.scenes;

import java.util.HashMap;
import java.util.Map;

public final class Scene {
    
    private final EntityManager entityManager;
    private final Map<Class<? extends Component>, ComponentManager<?>> componentManagers;

    public Scene() {
        entityManager = new EntityManager(this);
        componentManagers = createComponentManagersMap();
    }

    public Entity newEntity() {
        return entityManager.newEntity();
    }
    
    public Entity newEntity(String name) {
        return entityManager.newEntity(name);
    }

    public void destroy(String entityName) {
        entityManager.destroy(entityName);
    }

    public void destroy(Entity entity) {
        entityManager.destroy(entity);
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
