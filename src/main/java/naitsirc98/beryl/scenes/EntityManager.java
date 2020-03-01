package naitsirc98.beryl.scenes;

import naitsirc98.beryl.logging.Log;

import java.util.*;
import java.util.stream.Stream;

import static naitsirc98.beryl.scenes.Entity.UNNAMED;
import static naitsirc98.beryl.scenes.Entity.UNTAGGED;
import static naitsirc98.beryl.util.types.TypeUtils.getOrElse;

/**
 * The EntityManager handles the creation, storage, look-up and destruction of entities of a scene
 */
public final class EntityManager implements Iterable<Entity> {

    private static final int INITIAL_CAPACITY = 32;

    /** Scene of this EntityManager and thus the scene of all the entities created by it */
    private final Scene scene;
    /** List of entities. Slots with null values means recyclable positions */
    private final List<Entity> entities;
    /** Indices of free positions to recycle */
    private final Queue<Integer> freeIndices;
    /** Entity names look-up table */
    private final Map<String, Entity> nameTable;
    /** Entity tag look-up table */
    private final Map<String, List<Entity>> tagTable;

    /**
     * Instantiates a new Entity manager.
     *
     * @param scene the scene
     */
    EntityManager(Scene scene) {
        this.scene = scene;
        entities = new ArrayList<>(INITIAL_CAPACITY);
        freeIndices = new ArrayDeque<>(INITIAL_CAPACITY);
        nameTable = new HashMap<>(INITIAL_CAPACITY);
        tagTable = new HashMap<>(INITIAL_CAPACITY);
    }

    /**
     * Creates a new Entity with no name and no tag
     *
     * @return the entity
     */
    public Entity newEntity() {
        return newEntity(UNNAMED, UNTAGGED);
    }

    /**
     * Creates a new entity with the given name and tag. Only the name must be unique
     *
     * @param name the unique name of this entity
     * @param tag  the tag
     * @return the entity
     */
    public synchronized Entity newEntity(String name, String tag) {

        if(nameTable.containsKey(name)) {
            Log.error("There is already an Entity named " + name + " in this scene. Names must be unique per scene");
            return null;
        }

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

    /**
     * Removes the given entity
     *
     * @param entity the entity
     */
    public void remove(Entity entity) {

        if(!entity.name().equals(UNNAMED)) {
            nameTable.remove(entity.name());
        }

        if(!entity.tag().equals(UNTAGGED)) {
            tagTable.get(entity.tag()).remove(entity);
        }

        entities.set(entity.index(), null);
        freeIndices.add(entity.index());
    }

    /**
     * Finds the entity by its name
     *
     * @param name the name
     * @return the entity
     */
    public Entity find(String name) {
        return nameTable.get(name);
    }

    /**
     * Finds the first entity with the specified tag
     *
     * @param tag the tag
     * @return the with tag
     */
    public Entity findWithTag(String tag) {
        if(!tagTable.containsKey(tag)) {
            return null;
        }
        return tagTable.get(tag).get(0);
    }

    /**
     * Finds all the entities with the specified tag
     *
     * @param tag the tag
     * @return the all with tags
     */
    public Stream<Entity> findAllWithTags(String tag) {
        if(!tagTable.containsKey(tag)) {
            return null;
        }
        return tagTable.get(tag).stream();
    }

    /**
     * Tells whether exists an entity with the given name or not
     *
     * @param name the name
     * @return the boolean
     */
    public boolean exists(String name) {
        return nameTable.containsKey(name);
    }

    /**
     * Returns the number of entities alive
     *
     * @return the int
     */
    public int entityCount() {
        return entities.size() - freeIndices.size();
    }

    /**
     * Returns all the entities
     *
     * @return the stream
     */
    public Stream<Entity> entities() {
        return entities.stream().filter(Objects::nonNull);
    }

    @Override
    public Iterator<Entity> iterator() {
        return entities().iterator();
    }

    /**
     * Destroy all entities
     */
    void remove() {

        entities().forEach(Entity::onDestroy);

        entities.clear();
        freeIndices.clear();
        nameTable.clear();
        tagTable.clear();
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
