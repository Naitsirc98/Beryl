package naitsirc98.beryl.examples.pbr;

import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.core.DefaultConfigurations;
import naitsirc98.beryl.examples.common.CameraController;
import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.rendering.ShadingModel;
import naitsirc98.beryl.graphics.textures.Sampler;
import naitsirc98.beryl.graphics.textures.Texture.Quality;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.lights.PointLight;
import naitsirc98.beryl.materials.PBRMetallicMaterial;
import naitsirc98.beryl.meshes.SphereMesh;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.meshes.views.StaticMeshView;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.SceneManager;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.StaticMeshInstance;
import naitsirc98.beryl.scenes.environment.SceneEnvironment;
import naitsirc98.beryl.scenes.environment.skybox.Skybox;
import naitsirc98.beryl.scenes.environment.skybox.SkyboxFactory;
import naitsirc98.beryl.util.Color;

import java.nio.file.Path;

import static naitsirc98.beryl.scenes.Fog.DEFAULT_FOG_DENSITY;

public class PBRDemo extends BerylApplication {

    public PBRDemo() {
        BerylConfiguration.SHADOWS_ENABLED_ON_START.set(false);
        BerylConfiguration.DEFAULT_SHADING_MODEL.set(ShadingModel.PBR_METALLIC);
        BerylConfiguration.SET_CONFIGURATION_METHOD.set(DefaultConfigurations.debugConfiguration());
        BerylConfiguration.PRINT_SHADERS_SOURCE.set(false);
        BerylConfiguration.OPENGL_ENABLE_WARNINGS_UNIFORMS.set(false);
    }

    @Override
    protected void onStart() {

        Scene scene = SceneManager.newScene("PBR Demo");

        setupCamera(scene);

        setSceneEnvironment(scene);
        
        setSceneObjects(scene);

        SceneManager.setScene(scene);
    }

    private void setSceneObjects(Scene scene) {

        PBRSphere.create(scene, 0, 0, 0, BerylFiles.getPath("textures/rusted_iron"));

        PBRSphere.create(scene, 60, 0, 0, BerylFiles.getPath("textures/gold"));

    }

    private void setupCamera(Scene scene) {

        scene.camera().position(0, 0, -50);

        Entity cameraController = scene.newEntity();

        cameraController.add(CameraController.class);
    }

    private void setSceneEnvironment(Scene scene) {

        SceneEnvironment environment = scene.environment();

        Skybox skybox = SkyboxFactory.newSkyboxHDR("G:\\JavaDevelopment\\Quasar\\src\\main\\resources\\resources\\textures\\hdr\\newport_loft.hdr");

        PointLight light = new PointLight().position(scene.camera().position()).color(Color.colorWhite().intensify(10));

        environment.lighting().pointLights().add(light);
        environment.skybox(skybox);
        environment.ambientColor(new Color(0.5f));
        environment.fog().density(DEFAULT_FOG_DENSITY);
    }
}
