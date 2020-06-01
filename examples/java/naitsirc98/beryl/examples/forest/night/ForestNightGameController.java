package naitsirc98.beryl.examples.forest.night;

import naitsirc98.beryl.audio.AudioListener;
import naitsirc98.beryl.core.Time;
import naitsirc98.beryl.examples.forest.Helicopter;
import naitsirc98.beryl.input.Input;
import naitsirc98.beryl.input.Key;
import naitsirc98.beryl.scenes.Camera;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.audio.AudioPlayer;
import naitsirc98.beryl.scenes.components.behaviours.UpdateBehaviour;
import naitsirc98.beryl.scenes.environment.skybox.Skybox;
import naitsirc98.beryl.tasks.Task;
import naitsirc98.beryl.tasks.TaskManager;
import org.joml.Vector3f;

import static naitsirc98.beryl.examples.forest.night.ForestNight.FOREST_NIGHT_SOUND;
import static naitsirc98.beryl.util.Maths.radians;
import static naitsirc98.beryl.util.Maths.sin;

public class ForestNightGameController extends UpdateBehaviour {

    public static Entity create(Scene scene) {
        Entity entity = scene.newEntity();
        entity.add(ForestNightGameController.class);
        return entity;
    }

    private Vector3f lastPosition;
    private boolean helicopterSpawned;

    public ForestNightGameController() {

    }

    @Override
    protected void init() {
        super.init();
        lastPosition = new Vector3f();
    }

    @Override
    protected void onStart() {
        scene().entity(FOREST_NIGHT_SOUND).get(AudioPlayer.class).play();
        helicopterSpawned = false;
    }

    @Override
    public void onUpdate() {

        updateAudioListener();

        if(!helicopterSpawned && Input.isKeyTyped(Key.KEY_H)) {
            TaskManager.submitGraphicsTask(new Task() {
                @Override
                protected void perform() {
                    Helicopter.create(scene());
                }
            });
            // helicopterSpawned = true;
        }

        Skybox skybox = scene().environment().skybox();

        skybox.rotate(radians(0.0042f));
    }

    private void updateAudioListener() {

        Camera camera = scene().camera();

        AudioListener.get().position(camera.position());
        AudioListener.get().orientation(camera.forward(), camera.up());
        AudioListener.get().velocity(lastPosition.sub(camera.position()).negate());

        if(camera.position().y() < -4.0f) {
            scene().entity(FOREST_NIGHT_SOUND).get(AudioPlayer.class).source().pitch(0.5f);
        } else {
            scene().entity(FOREST_NIGHT_SOUND).get(AudioPlayer.class).source().pitch(1.0f);
        }

        lastPosition.set(camera.position());
    }
}
