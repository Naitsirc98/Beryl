package naitsirc98.beryl.examples.forest;

import naitsirc98.beryl.audio.AudioListener;
import naitsirc98.beryl.core.Time;
import naitsirc98.beryl.examples.forest.day.ForestDay;
import naitsirc98.beryl.input.Input;
import naitsirc98.beryl.input.Key;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.scenes.Camera;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.environment.skybox.Skybox;
import naitsirc98.beryl.scenes.components.audio.AudioPlayer;
import naitsirc98.beryl.scenes.components.behaviours.UpdateBehaviour;
import naitsirc98.beryl.scenes.components.meshes.StaticMeshInstance;
import naitsirc98.beryl.tasks.Task;
import naitsirc98.beryl.tasks.TaskManager;
import naitsirc98.beryl.util.Color;
import org.joml.Vector3f;

import static naitsirc98.beryl.examples.forest.day.ForestDay.FOREST_DAY_SOUND;
import static naitsirc98.beryl.examples.forest.day.ForestDay.FOREST_NIGHT_SOUND;
import static naitsirc98.beryl.examples.forest.Lamp.LAMP_NAME;
import static naitsirc98.beryl.util.Maths.*;

public class GameController extends UpdateBehaviour {

    public static Entity create(Scene scene) {
        Entity entity = scene.newEntity();
        entity.add(GameController.class);
        return entity;
    }

    private Vector3f lastPosition;
    private boolean helicopterSpawned;

    public GameController() {

    }

    @Override
    protected void init() {
        super.init();
        lastPosition = new Vector3f();
    }

    @Override
    protected void onStart() {
        scene().entity(FOREST_DAY_SOUND).get(AudioPlayer.class).play();
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

        final float time = Math.abs(sin((Time.minutes() - 0.3f) * 0));

        Skybox skybox = scene().environment().skybox();

        // TODO
        skybox.rotate(radians(0.0042f));

        skybox.textureBlendFactor(time);

        DirectionalLight sun = scene().environment().lighting().directionalLight();

        sun.color().set(1.0f - time, 1.0f);

        // sun.direction(new Vector3f(-0.365f, -0.808f, 0.462f).rotateX(time * 10));

        scene().entity(FOREST_DAY_SOUND).get(AudioPlayer.class).source().gain(1.0f - time * 2);
        scene().entity(FOREST_NIGHT_SOUND).get(AudioPlayer.class).source().gain(time * time);

        scene().environment().fog().density(time / 2.0f);

        scene().environment().lighting().pointLights().get(0).color().set(time * 1.25f, 1.0f);

        scene().environment().ambientColor().set(clamp(0.2f, 1.0f, 1.0f - time), 1.0f);

        PhongMaterial lampMaterial = (PhongMaterial) scene().entity(LAMP_NAME).get(StaticMeshInstance.class).meshView().material();
        lampMaterial.emissiveColor(new Color().set(clamp(0.2f, 2.0f, time * 1.25f), 1.0f));
    }

    private void updateAudioListener() {

        Camera camera = scene().camera();

        AudioListener.get().position(camera.position());
        AudioListener.get().orientation(camera.forward(), camera.up());
        AudioListener.get().velocity(lastPosition.sub(camera.position()).negate());

        if(camera.position().y() < -4.0f) {
            scene().entity(ForestDay.FOREST_DAY_SOUND).get(AudioPlayer.class).source().pitch(0.5f);
        } else {
            scene().entity(ForestDay.FOREST_DAY_SOUND).get(AudioPlayer.class).source().pitch(1.0f);
        }

        lastPosition.set(camera.position());
    }
}
