package naitsirc98.beryl.examples.room;

import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.core.BerylConfigurationHelper;
import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.examples.common.CameraController;
import naitsirc98.beryl.graphics.rendering.ShadingModel;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.environment.SceneEnvironment;
import naitsirc98.beryl.scenes.environment.skybox.SkyboxFactory;
import naitsirc98.beryl.util.Color;
import org.joml.Vector3f;

public class RoomScene extends BerylApplication {

    public RoomScene() {
        BerylConfiguration.SHADOWS_ENABLED_ON_START.set(true);
        BerylConfiguration.SCENE_SHADING_MODEL.set(ShadingModel.PBR_METALLIC);
        BerylConfiguration.FIRST_SCENE_NAME.set("Room");
        BerylConfigurationHelper.debugConfiguration();
    }

    @Override
    protected void onStart(Scene scene) {

        Entity camera = scene.newEntity("Camera");
        camera.add(CameraController.class);

        scene.camera().position(-87.931f, 40.651f, 72.491f).lookAt(70, 0);

        Room.create(scene, new Vector3f(0, -2, 0));

        setupEnvironment(scene);
    }

    private void setupEnvironment(Scene scene) {

        SceneEnvironment environment = scene.environment();

        environment.skybox(SkyboxFactory.newSkybox(BerylFiles.getPath("textures/skybox/day")));

        environment.lighting().directionalLight(new DirectionalLight()
                .direction(-3.673E-1f, -3.019E-1f, -8.797E-1f)
                .color(Color.colorWhite().intensify(10)));
    }


}
