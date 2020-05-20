package naitsirc98.beryl.examples.pbr;

import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.core.DefaultConfigurations;
import naitsirc98.beryl.examples.common.CameraController;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.SceneManager;
import naitsirc98.beryl.scenes.environment.SceneEnvironment;
import naitsirc98.beryl.scenes.environment.skybox.Skybox;
import naitsirc98.beryl.scenes.environment.skybox.SkyboxFactory;
import naitsirc98.beryl.util.Color;

import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.scenes.Fog.DEFAULT_FOG_DENSITY;

public class PBRDemo extends BerylApplication {

    public PBRDemo() {
        BerylConfiguration.SET_CONFIGURATION_METHOD.set(DefaultConfigurations.debugConfiguration());
    }

    @Override
    protected void onStart() {

        Scene scene = new Scene("PBR Demo");

        setupCamera(scene);

        setSceneEnvironment(scene);

        SceneManager.setScene(scene);

        new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(BerylFiles.getPath("shaders/pbr/metallic/pbr_metallic.vert")))
                .attach(new GLShader(FRAGMENT_STAGE).source(BerylFiles.getPath("shaders/pbr/metallic/pbr_metallic.frag")))
                .link();
    }

    private void setupCamera(Scene scene) {

        Entity cameraController = scene.newEntity();

        cameraController.add(CameraController.class);
    }

    private void setSceneEnvironment(Scene scene) {

        SceneEnvironment environment = scene.environment();

        Skybox skybox = SkyboxFactory.newSkyboxHDR("G:\\JavaDevelopment\\Quasar\\src\\main\\resources\\resources\\textures\\hdr\\newport_loft.hdr");

        environment.skybox(skybox);
        environment.ambientColor(new Color(0.8f));
        environment.fog().density(DEFAULT_FOG_DENSITY);
    }
}
