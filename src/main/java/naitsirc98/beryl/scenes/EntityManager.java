package naitsirc98.beryl.scenes;

import java.util.*;

import static naitsirc98.beryl.scenes.Entity.UNNAMED;

public final class EntityManager {

    private static final int INITIAL_CAPACITY = 32;

    /** Scene of this EntityManager */
    private final Scene scene;
    /** List of entities. */
    private final List<Entity> entities;
    /** Indices of free positions to recycle */
    private final Queue<Integer> freeIndices;
    /** Entity names look-up table */
    private final Map<String, Entity> nameTable;

    EntityManager(Scene scene) {
        this.scene = scene;
        entities = new ArrayList<>(INITIAL_CAPACITY);
        freeIndices = new ArrayDeque<>(INITIAL_CAPACITY);
        nameTable = new HashMap<>(INITIAL_CAPACITY);
    }

    public Entity newEntity() {
        return newEntity(UNNAMED);
    }

    public Entity newEntity(String name) {

        Entity entity;

        if(!freeIndices.isEmpty()) {

            entity = recycle(name, freeIndices.poll());

        } else {

            entity = newEntity(name, entities.size());
            entities.add(entity);
        }

        if(!UNNAMED.equals(name)) {
            nameTable.put(name, entity);
        }

        return entity;
    }

    public void destroy(Entity entity) {

        if(!entity.name().equals(UNNAMED)) {
            nameTable.remove(entity.name());
        }

        entities.set(entity.index(), null);
        freeIndices.add(entity.index());

        entity.onDestroy();
    }

    public Entity get(String name) {
        return nameTable.get(name);
    }

    public boolean exists(String name) {
        return nameTable.containsKey(name);
    }

    public int count() {
        return (int) entities.stream().filter(Objects::nonNull).count();
    }

    private Entity recycle(String name, int index) {

        Entity entity = newEntity(name, index);

        entities.set(index, entity);

        return entity;
    }

    private Entity newEntity(String name, int handle) {
        return new Entity(name, scene, handle);
    }

}
