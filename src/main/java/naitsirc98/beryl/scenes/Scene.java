package naitsirc98.beryl.scenes;

import naitsirc98.beryl.graphics.rendering.RenderSystem;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.scenes.components.animations.Animator;
import naitsirc98.beryl.scenes.components.animations.AnimatorManager;
import naitsirc98.beryl.scenes.components.audio.AudioPlayer;
import naitsirc98.beryl.scenes.components.audio.AudioPlayerManager;
import naitsirc98.beryl.scenes.components.behaviours.AbstractBehaviour;
import naitsirc98.beryl.scenes.components.behaviours.BehaviourManager;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.math.TransformManager;
import naitsirc98.beryl.scenes.components.meshes.MeshInstance;
import naitsirc98.beryl.scenes.components.meshes.MeshInstanceManager;
import naitsirc98.beryl.scenes.components.meshes.SceneMeshInfo;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.scenes.Entity.UNTAGGED;
import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static naitsirc98.beryl.util.types.TypeUtils.newInstance;

public final class Scene {

    private final String name;
    
    private final EntityManager entityManager;

    private final SceneEnvironment environment;
    private final SceneEnhancedWater enhancedWater;

    private final Camera camera;
    private final SceneCameraInfo cameraInfo;

    // === Component Managers
    private final TransformManager transforms;
    private final BehaviourManager behaviours;
    private final MeshInstanceManager meshes;
    private final AudioPlayerManager audio;
    private final AnimatorManager animators;

    private final Map<Class<? extends Component>, ComponentManager<?>> componentManagers;
    // ===

    private final Deque<Runnable> taskQueue;

    private boolean started;

    public Scene(String name) {

        this.name = requireNonNull(name);

        entityManager = new EntityManager(this);

        environment = new SceneEnvironment();

        enhancedWater = new SceneEnhancedWater();

        cameraInfo = new SceneCameraInfo();

        camera = new Camera(cameraInfo);

        // === Component Managers
        transforms = newInstance(TransformManager.class, this);
        behaviours = newInstance(BehaviourManager.class, this);
        meshes = newInstance(MeshInstanceManager.class, this);
        audio = newInstance(AudioPlayerManager.class, this);
        animators = newInstance(AnimatorManager.class, this);

        componentManagers = createComponentManagersMap();
        // ===

        taskQueue = new ArrayDeque<>();
    }

    public String name() {
        return name;
    }

    public SceneEnvironment environment() {
        return environment;
    }

    public SceneEnhancedWater enhancedWater() {
        return enhancedWater;
    }

    public SceneMeshInfo meshInfo() {
        return meshes;
    }

    public SceneCameraInfo cameraInfo() {
        return cameraInfo;
    }

    public List<Animator> animators() {
        return animators.list();
    }

    void start() {
        processTasks();
        started = true;
    }

    void update() {
        behaviours.update();
    }

    void processTasks() {
        while(!taskQueue.isEmpty()) {
            taskQueue.poll().run();
        }
    }

    void lateUpdate() {
        behaviours.lateUpdate();
    }

    void endUpdate() {

        if(camera.modified()) {
            camera.updateMatrices();
        }

        transforms.update();
        animators.update();
        environment.update();

        RenderSystem.prepare(this);
    }

    void render() {
        RenderSystem.render(this);
    }

    void terminate() {
        // TODO
        processTasks();
        entityManager.remove();
        componentManagers.values().forEach(ComponentManager::removeAll);
        componentManagers.clear();
        environment.release();
    }

    public boolean started() {
        return started;
    }

    public void submit(Runnable task) {

        if(task == null) {
            Log.error("Cannot submit a null task");
            return;
        }

        taskQueue.add(task);
    }

    public Camera camera() {
        return camera;
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
    <T extends Component> void destroyComponent(T component) {

        ComponentManager<T> manager = managerOf(component.type());

        manager.remove(component);

        component.delete();
    }

    @SuppressWarnings("unchecked")
    private <T extends Component> ComponentManager<T> managerOf(Class<T> type) {
        assertTrue(componentManagers.containsKey(type));
        return (ComponentManager<T>) componentManagers.get(type);
    }

    private Map<Class<? extends Component>, ComponentManager<?>> createComponentManagersMap() {

        Map<Class<? extends Component>, ComponentManager<?>> components = new HashMap<>();

        components.put(AbstractBehaviour.class, behaviours);
        components.put(Transform.class, transforms);
        components.put(MeshInstance.class, meshes);
        components.put(AudioPlayer.class, audio);
        components.put(Animator.class, animators);

        return components;
    }
}
