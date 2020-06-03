package naitsirc98.beryl.examples.forest.night;

import naitsirc98.beryl.audio.AudioClip;
import naitsirc98.beryl.audio.AudioDistanceModel;
import naitsirc98.beryl.audio.AudioSystem;
import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.core.BerylConfigurationHelper;
import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.examples.common.CameraController;
import naitsirc98.beryl.examples.forest.*;
import naitsirc98.beryl.graphics.rendering.ShadingModel;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.lights.LightRange;
import naitsirc98.beryl.lights.PointLight;
import naitsirc98.beryl.lights.SpotLight;
import naitsirc98.beryl.scenes.Camera;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.audio.AudioPlayer;
import naitsirc98.beryl.scenes.components.behaviours.UpdateMutableBehaviour;
import naitsirc98.beryl.scenes.environment.SceneEnvironment;
import naitsirc98.beryl.scenes.environment.SceneLighting;
import naitsirc98.beryl.scenes.environment.skybox.Skybox;
import naitsirc98.beryl.scenes.environment.skybox.SkyboxFactory;
import naitsirc98.beryl.util.Color;
import org.joml.Vector3f;

import java.nio.file.Paths;

import static naitsirc98.beryl.examples.forest.Terrain.TERRAIN_SIZE;

public class ForestNight extends BerylApplication {

    public static final String FOREST_NIGHT_SOUND = "Forest night sound";

    public ForestNight() {
        BerylConfiguration.SHADOWS_ENABLED_ON_START.set(false);
        BerylConfiguration.SCENE_SHADING_MODEL.set(ShadingModel.PHONG);
        BerylConfiguration.FIRST_SCENE_NAME.set("Forest Scene");
        BerylConfigurationHelper.developmentConfiguration();
    }

    @Override
    protected void onStart(Scene scene) {

        Window.get().center();

        setupScene(scene);
    }

    private void setupScene(Scene scene) {

        setSceneEnvironment(scene);

        initSceneCamera(scene);

        setEnvironmentSounds(scene);

        ForestNightGameController.create(scene);

        Terrain.create(scene);

        final float waterSurfaceY = -5.0f;

        Water.create(scene, TERRAIN_SIZE / 2, waterSurfaceY, TERRAIN_SIZE / 2, TERRAIN_SIZE / 2);

        Grass.placeGrassUnderWater(scene, Terrain.getTerrainMesh(), (int) TERRAIN_SIZE, 8.0f, waterSurfaceY, 100);

        Lamp.create(scene, 473.74f, 0.067f, 376.301f, 4.0f);

        Tree.createRandomForest(scene, Terrain.getTerrainMesh(), (int) TERRAIN_SIZE, -4.0f, 500);

        Helicopter.getHelicopterModel();
    }

    private void setEnvironmentSounds(Scene scene) {

        AudioSystem.distanceModel(AudioDistanceModel.EXPONENT_DISTANCE);

        Entity forestNightSound = scene.newEntity(FOREST_NIGHT_SOUND);
        AudioPlayer forestNightAudioPlayer = forestNightSound.add(AudioPlayer.class);
        forestNightAudioPlayer.source()
                .minGain(0)
                .maxGain(1)
                .gain(1)
                .rollOff(1)
                .position(new Vector3f(TERRAIN_SIZE/2, 0, TERRAIN_SIZE/2))
                .looping(true);

        forestNightAudioPlayer.clip(AudioClip.get("forestNight", params -> params.audioFile(BerylFiles.getPath("audio/forest_night.ogg"))));
    }

    private void initSceneCamera(Scene scene) {

        Camera camera = scene.camera();
        camera.lookAt(0, 0).position(391, 13.0f, 479);

        Entity cameraController = scene.newEntity();
        cameraController.add(CameraController.class);
    }

    private void setSceneEnvironment(Scene scene) {

        SceneEnvironment environment = scene.environment();

        Skybox skybox = SkyboxFactory.newSkybox(Paths.get("C:\\Users\\naits\\Downloads\\skybox (2)"));
        environment.skybox(skybox);

        Color fogColor = Color.colorWhite().intensify(0.15f);

        environment.clearColor(fogColor);

        environment.ambientColor(fogColor);
        environment.fog().color(fogColor).density(0.8f);

        setSceneLights(scene);
    }

    private void setSceneLights(Scene scene) {

        SceneLighting lighting = scene.environment().lighting();

        // DirectionalLight sun = new DirectionalLight();

        // lighting.directionalLight(sun);
        // lighting.directionalLight().direction(-0.365f, -0.808f, 0.462f);

        lighting.pointLights().add(new PointLight()
                .position(471.379f, 4.051f, 375.764f)
                .color(Color.colorWhite().intensify(2))
                .range(LightRange.MEDIUM));

        SpotLight flashLight = new SpotLight().range(LightRange.MEDIUM).color(Color.colorWhite().intensify(2));

        lighting.spotLights().add(flashLight);

        Entity flashLightController = scene.newEntity();
        flashLightController.add(UpdateMutableBehaviour.class).onUpdate(self -> {
            flashLight.position(scene.camera().position()).direction(scene.camera().forward());
        });
    }
}
