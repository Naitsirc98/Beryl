package naitsirc98.beryl.scenes;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.Singleton;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Stream;

import static naitsirc98.beryl.util.TypeUtils.getOrElse;

public final class SceneManager extends BerylSystem {

    public static final boolean DEBUG_REPORT_ENABLED = BerylConfiguration.SCENES_DEBUG_REPORT.get(Beryl.DEBUG);

    @Singleton
    private static SceneManager instance;

    public static Scene newScene() {
        return newScene(AddMode.LAST);
    }

    public static Scene newScene(AddMode mode) {
        Scene scene = new Scene();
        addScene(scene, mode);
        return scene;
    }

    public static boolean withinScenePass() {
        return activeScene() != null;
    }

    public static Scene activeScene() {
        return instance.activeScene;
    }

    public static Scene scene() {
        return getOrElse(activeScene(), frontScene());
    }

    public static Scene frontScene() {
        return instance.scenes.peekFirst();
    }

    public static Scene backScene() {
        return instance.scenes.peekLast();
    }

    public static void setScene(Scene scene) {

        if(calledWithinScenePass()) {
            return;
        }

        if(notSuitable(scene)) {
            return;
        }

        instance.terminateAllScenes();

        addScene(scene);
    }

    public static void addScene(Scene scene) {
        addScene(scene, AddMode.LAST);
    }

    public static void addScene(Scene scene, AddMode mode) {

        if(calledWithinScenePass()) {
            return;
        }

        if(notSuitable(scene)) {
            return;
        }

        if(mode == null) {
            Log.error("AddMode cannot be null");
            return;
        }

        if(mode == AddMode.LAST) {
            instance.scenes.addLast(scene);
        } else {
            instance.scenes.addFirst(scene);
        }
    }

    public static int sceneCount() {
        return instance.scenes.size();
    }

    public static Stream<Scene> scenes() {
        return instance.scenes.stream();
    }

    private static boolean notSuitable(Scene scene) {
        if(scene == null) {
            Log.error("Cannot add a null scene");
            return true;
        }
        if(instance.scenes.contains(scene)) {
            Log.error("Scene " + scene + " is already added");
            return true;
        }
        return false;
    }

    private static boolean calledWithinScenePass() {

        if(withinScenePass()) {
            Log.error("Cannot perform SceneManager operations within a scene update");
            return true;
        }

        return false;
    }

    private Scene activeScene;
    private final Deque<Scene> scenes;

    private SceneManager() {
        scenes = new ArrayDeque<>(2);
    }

    @Override
    protected void init() {

    }

    @Override
    protected void terminate() {
        terminateAllScenes();
    }

    public void update() {
        for(Scene scene : scenes) {
            update(activeScene = scene);
        }
        activeScene = null;
    }

    private void update(Scene scene) {
        scene.update();
        scene.processTasks();
        scene.lateUpdate();
        scene.processTasks();
    }

    public void render() {
        for(Scene scene : scenes) {
            render(activeScene = scene);
        }
        activeScene = null;
    }

    private void render(Scene scene) {
        scene.render();
    }

    private void terminateAllScenes() {
        while(!scenes.isEmpty()) {
            scenes.poll().terminate();
        }
    }

    @Override
    public CharSequence debugReport() {

        StringBuilder builder = new StringBuilder();

        int index = 0;

        for(Scene scene : scenes) {

            builder.append("\n\t\t").append("[SCENE ").append(index++).append("]: ");

            builder.append("Entity count = ").append(scene.entityCount());
            builder.append(" | Component count = ").append(scene.componentCount());
        }

        return builder;
    }

    public enum AddMode {

        FIRST,
        LAST

    }
}
