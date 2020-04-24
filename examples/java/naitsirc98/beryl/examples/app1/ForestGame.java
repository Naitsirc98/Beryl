package naitsirc98.beryl.examples.app1;

import naitsirc98.beryl.audio.AudioClip;
import naitsirc98.beryl.audio.AudioDistanceModel;
import naitsirc98.beryl.audio.AudioSystem;
import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.GraphicsAPI;
import naitsirc98.beryl.graphics.window.DisplayMode;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.lights.LightRange;
import naitsirc98.beryl.lights.PointLight;
import naitsirc98.beryl.scenes.*;
import naitsirc98.beryl.scenes.components.audio.AudioPlayer;
import naitsirc98.beryl.util.Color;
import org.joml.Vector3f;

import java.util.Random;

import static naitsirc98.beryl.examples.app1.Terrain.TERRAIN_SIZE;
import static naitsirc98.beryl.scenes.Fog.DEFAULT_FOG_DENSITY;
import static naitsirc98.beryl.scenes.SceneManager.newScene;


public class ForestGame extends BerylApplication {

    public static final String FOREST_DAY_SOUND = "Forest day sound";
    public static final String FOREST_NIGHT_SOUND = "Forest night sound";
    private static final Random RAND = new Random(System.nanoTime());

    public static void main(String[] args) {

        BerylConfiguration.SET_CONFIGURATION_METHOD.set(ForestGame::setConfiguration);

        Beryl.launch(new ForestGame());
    }

    private static void setConfiguration() {
        BerylConfiguration.DEBUG.set(true);
        BerylConfiguration.INTERNAL_DEBUG.set(true);
        BerylConfiguration.SHOW_DEBUG_INFO.set(true);
        BerylConfiguration.GRAPHICS_API.set(GraphicsAPI.OPENGL);
        BerylConfiguration.WINDOW_DISPLAY_MODE.set(DisplayMode.WINDOWED);
        BerylConfiguration.VSYNC.set(false);
    }

    private ForestGame() {

    }

    @Override
    protected void onStart() {

        Window.get().center();

        createScene();
    }

    private void createScene() {

        Scene scene = newScene("Forest Scene");

        setSceneEnvironment(scene);

        initSceneCamera(scene);

        setEnvironmentSounds(scene);

        GameController.create(scene);

        Terrain.create(scene);

        Water.create(scene, TERRAIN_SIZE / 2, -5.0f, TERRAIN_SIZE / 2, TERRAIN_SIZE / 2);

        Tree.createRandomForest(scene, Terrain.getTerrainMesh(), (int) TERRAIN_SIZE, -4.0f, 800);

        // Grass.placeGrassAtRandomPositions(scene, Terrain.getTerrainMesh(), (int) TERRAIN_SIZE, 8.0f, 100);

        Lamp.create(scene, 473.74f, 0.067f, 376.301f, 4.0f);

        Helicopter.getHelicopterModel();
    }

    private void setEnvironmentSounds(Scene scene) {

        AudioSystem.distanceModel(AudioDistanceModel.EXPONENT_DISTANCE);

        Entity forestSound = scene.newEntity(FOREST_DAY_SOUND);
        AudioPlayer forestAudioPlayer = forestSound.add(AudioPlayer.class);
        forestAudioPlayer.source()
                .minGain(0)
                .maxGain(1)
                .gain(0.8f)
                .rollOff(1)
                .position(new Vector3f(TERRAIN_SIZE/2, 0, TERRAIN_SIZE/2))
                .looping(true);

        forestAudioPlayer.source().buffer(AudioClip.get("forest", params -> params.audioFile("G:\\__inglesjavi\\forest2.ogg")).buffer());

        Entity forestNightSound = scene.newEntity(FOREST_NIGHT_SOUND);
        AudioPlayer forestNightAudioPlayer = forestNightSound.add(AudioPlayer.class);
        forestNightAudioPlayer.source()
                .minGain(0)
                .maxGain(1)
                .gain(0)
                .rollOff(1)
                .position(new Vector3f(TERRAIN_SIZE/2, 0, TERRAIN_SIZE/2))
                .looping(true);

        forestNightAudioPlayer.source().buffer(AudioClip.get("forestNight", params -> params.audioFile("G:\\__inglesjavi\\forest_night.ogg")).buffer());
    }

    private void initSceneCamera(Scene scene) {

        Camera camera = scene.camera();
        camera.lookAt(0, 0).position(391, 13.0f, 479).farPlane(10000);

        Entity cameraController = scene.newEntity();
        cameraController.add(CameraController.class);
    }

    private void setSceneEnvironment(Scene scene) {

        SceneEnvironment environment = scene.environment();

        environment.skybox(new Skybox(BerylFiles.getString("textures/skybox/day"), BerylFiles.getString("textures/skybox/night")));
        environment.ambientColor(Color.colorWhite());
        environment.fog().density(DEFAULT_FOG_DENSITY);

        setSceneLights(scene);
    }

    private void setSceneLights(Scene scene) {

        SceneLighting lighting = scene.environment().lighting();

        lighting.directionalLight(new DirectionalLight());
        lighting.directionalLight().direction(-0.453f, -0.902f, 0.391f);

        lighting.pointLights().add(new PointLight()
                .position(471.379f, 4.051f, 375.764f)
                .color(Color.colorBlack())
                .range(LightRange.MEDIUM));
    }
}
