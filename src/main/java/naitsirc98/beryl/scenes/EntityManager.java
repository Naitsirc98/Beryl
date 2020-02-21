package naitsirc98.beryl.scenes;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

import static naitsirc98.beryl.scenes.Entity.UNNAMED;
import static naitsirc98.beryl.scenes.Entity.UNTAGGED;
import static naitsirc98.beryl.util.TypeUtils.getOrElse;

public final class EntityManager implements Iterable<Entity> {

    private static final int INITIAL_CAPACITY = 32;

    /** Scene of this EntityManager */
    private final Scene scene;
    /** List of entities. */
    private final List<Entity> entities;
    /** Indices of free positions to recycle */
    private final Queue<Integer> freeIndices;
    /** Entity names look-up table */
    private final Map<String, Entity> nameTable;
    /** Entity tag look-up table */
    private final Map<String, List<Entity>> tagTable;

    EntityManager(Scene scene) {
        this.scene = scene;
        entities = new ArrayList<>(INITIAL_CAPACITY);
        freeIndices = new ArrayDeque<>(INITIAL_CAPACITY);
        nameTable = new HashMap<>(INITIAL_CAPACITY);
        tagTable = new HashMap<>(INITIAL_CAPACITY);
    }

    public Entity newEntity() {
        return newEntity(UNNAMED, UNTAGGED);
    }

    public synchronized Entity newEntity(String name, String tag) {

        Entity entity;

        if(!freeIndices.isEmpty()) {

            entity = recycle(name, tag, freeIndices.poll());

        } else {

            entity = newEntity(name, tag, entities.size());
            entities.add(entity);
        }

        if(!Objects.equals(getOrElse(name, UNNAMED), UNNAMED)) {
            nameTable.put(name, entity);
        }

        if(!Objects.equals(getOrElse(tag, UNTAGGED), UNTAGGED)) {
            putInTagTable(tag, entity);
        }

        return entity;
    }

    private void putInTagTable(String tag, Entity entity) {
        tagTable.computeIfAbsent(tag, k -> new ArrayList<>()).add(entity);
    }

    public void destroy(Entity entity) {

        if(!entity.name().equals(UNNAMED)) {
            nameTable.remove(entity.name());
        }

        if(!entity.tag().equals(UNTAGGED)) {
            tagTable.get(entity.tag()).remove(entity);
        }

        entities.set(entity.index(), null);
        freeIndices.add(entity.index());

        entity.onDestroy();
    }

    public Entity get(String name) {
        return nameTable.get(name);
    }

    public Entity getWithTag(String tag) {
        if(!tagTable.containsKey(tag)) {
            return null;
        }
        return tagTable.get(tag).get(0);
    }

    public Stream<Entity> getAllWithTags(String tag) {
        if(!tagTable.containsKey(tag)) {
            return null;
        }
        return tagTable.get(tag).stream();
    }

    public boolean exists(String name) {
        return nameTable.containsKey(name);
    }

    public int entityCount() {
        return (int) entities.stream().filter(Objects::nonNull).count();
    }

    public Stream<Entity> entities() {
        return entities.stream().filter(Objects::nonNull);
    }

    @NotNull
    @Override
    public Iterator<Entity> iterator() {
        return entities().iterator();
    }

    private Entity recycle(String name, String tag, int index) {

        Entity entity = newEntity(name, tag, index);

        entities.set(index, entity);

        return entity;
    }

    private Entity newEntity(String name, String tag, int handle) {
        return new Entity(name, tag, scene, handle);
    }

}
