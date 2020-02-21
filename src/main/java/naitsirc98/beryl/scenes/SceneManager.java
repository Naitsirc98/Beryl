package naitsirc98.beryl.scenes;

import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.core.Log;
import naitsirc98.beryl.util.Singleton;

import java.util.ArrayDeque;
import java.util.Deque;

public final class SceneManager extends BerylSystem {

    @Singleton
    private static SceneManager instance;

    public static Scene activeScene() {
        return instance.activeScene;
    }

    public static void setScene(Scene scene) {

        if(calledWithinSceneUpdate()) {
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

        if(calledWithinSceneUpdate()) {
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

    private static boolean calledWithinSceneUpdate() {

        if(activeScene() != null) {
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

    public enum AddMode {

        FIRST,
        LAST

    }
}
