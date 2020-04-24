package naitsirc98.beryl.examples.app1;

import naitsirc98.beryl.core.Time;
import naitsirc98.beryl.input.Input;
import naitsirc98.beryl.input.Key;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.Skybox;
import naitsirc98.beryl.scenes.components.audio.AudioPlayer;
import naitsirc98.beryl.scenes.components.behaviours.UpdateBehaviour;
import naitsirc98.beryl.scenes.components.meshes.StaticMeshInstance;

import static naitsirc98.beryl.examples.app1.ForestGame.FOREST_DAY_SOUND;
import static naitsirc98.beryl.examples.app1.ForestGame.FOREST_NIGHT_SOUND;
import static naitsirc98.beryl.examples.app1.Lamp.LAMP_NAME;
import static naitsirc98.beryl.util.Maths.*;
import static org.lwjgl.opengl.GL11C.glFinish;

public class GameController extends UpdateBehaviour {

    public static Entity create(Scene scene) {
        Entity entity = scene.newEntity();
        entity.add(GameController.class);
        return entity;
    }

    private boolean helicopterSpawned;

    @Override
    protected void onStart() {
        scene().entity(FOREST_DAY_SOUND).get(AudioPlayer.class).play();
        scene().entity(FOREST_NIGHT_SOUND).get(AudioPlayer.class).play();
        helicopterSpawned = false;
    }

    @Override
    public void onUpdate() {

        if(!helicopterSpawned && Input.isKeyTyped(Key.KEY_H)) {
            Helicopter.create(scene());
            // helicopterSpawned = true;
        }

        final float time = Math.abs(sin((Time.minutes() - 0.3f) * 0));

        Skybox skybox = scene().environment().skybox();

        skybox.rotate(radians(0.004f));

        skybox.textureBlendFactor(time);

        DirectionalLight sun = scene().environment().lighting().directionalLight();

        sun.color().set(1.0f - time, 1.0f);

        scene().entity(FOREST_DAY_SOUND).get(AudioPlayer.class).source().gain(1.0f - time * 2);
        scene().entity(FOREST_NIGHT_SOUND).get(AudioPlayer.class).source().gain(time * time);

        scene().environment().fog().density(time / 2.0f);

        scene().environment().lighting().pointLights().get(0).color().set(time * 1.25f, 1.0f);

        scene().environment().ambientColor().set(clamp(0.2f, 0.9f, 1.0f - time), 1.0f);

        PhongMaterial lampMaterial = (PhongMaterial) scene().entity(LAMP_NAME).get(StaticMeshInstance.class).meshView().material();
        lampMaterial.emissiveColor().set(clamp(0.2f, 2.0f, time * 1.25f), 1.0f);
        lampMaterial.modify();
    }
}