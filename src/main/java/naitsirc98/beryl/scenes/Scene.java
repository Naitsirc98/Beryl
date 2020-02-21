package naitsirc98.beryl.scenes;

public final class Scene {
    
    private final EntityManager entityManager;

    public Scene() {
        entityManager = new EntityManager(this);
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

    public void destroy(Component component) {

    }

}
