package naitsirc98.beryl.scenes;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.types.Singleton;

public final class SceneManager extends BerylSystem {

    public static final boolean DEBUG_REPORT_ENABLED = BerylConfiguration.SCENES_DEBUG_REPORT.getOrDefault(Beryl.DEBUG);

    @Singleton
    private static SceneManager instance;

    public static Scene newScene(String name) {
        Scene scene = new Scene(name);
        setScene(scene);
        return scene;
    }

    public static boolean withinScenePass() {
        return scene() != null;
    }

    public static Scene scene() {
        return instance.scene;
    }

    public static void setScene(Scene scene) {

        if(calledWithinScenePass()) {
            return;
        }

        if(notSuitable(scene)) {
            return;
        }

        if(instance.scene != null) {
            instance.scene.terminate();
        }

        instance.scene = scene;
    }

    private Scene scene;

    private SceneManager() {

    }

    @Override
    protected void init() {

    }

    @Override
    protected void terminate() {
        scene.terminate();
    }

    public void update() {

        final Scene scene = this.scene;

        if(!scene.started()) {
            scene.start();
        }

        scene.update();
        scene.processTasks();

        scene.lateUpdate();
        scene.processTasks();
    }

    public void endUpdate() {
        scene.endUpdate();
    }

    public void render() {
        scene.render();
    }

    @Override
    public CharSequence debugReport() {

        StringBuilder builder = new StringBuilder();

        builder.append("\n\t\t").append("[SCENE '").append(scene.name()).append("']: ");

        builder.append("Entity count = ").append(scene.entityCount());
        builder.append(" | Component count = ").append(scene.componentCount());

        return builder;
    }

    private static boolean notSuitable(Scene scene) {
        if(scene == null) {
            Log.error("Cannot add a null scene");
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
}
