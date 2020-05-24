package naitsirc98.beryl.examples.pbr;

import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.core.BerylConfigurationHelper;
import naitsirc98.beryl.examples.common.CameraController;
import naitsirc98.beryl.examples.forest.Water;
import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.rendering.ShadingModel;
import naitsirc98.beryl.graphics.textures.Sampler;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.materials.WaterMaterial;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.meshes.views.WaterMeshView;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.SceneManager;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.WaterMeshInstance;
import naitsirc98.beryl.scenes.environment.SceneEnvironment;
import naitsirc98.beryl.scenes.environment.skybox.Skybox;
import naitsirc98.beryl.scenes.environment.skybox.SkyboxFactory;
import naitsirc98.beryl.util.Color;
import org.joml.Vector3f;

import static naitsirc98.beryl.util.Maths.radians;

public class PBRDemo extends BerylApplication {

    public PBRDemo() {
        BerylConfiguration.SHADOWS_ENABLED_ON_START.set(false);
        BerylConfiguration.SCENE_SHADING_MODEL.set(ShadingModel.PBR_METALLIC);
        BerylConfigurationHelper.debugReleaseConfiguration();
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

        CerberusRevolver.create(scene, new Vector3f(150, 0, 0), 0.85f);
    }

    private void setupCamera(Scene scene) {

        scene.camera().position(0, 0, -50);

        Entity cameraController = scene.newEntity();

        cameraController.add(CameraController.class);
    }

    private void setSceneEnvironment(Scene scene) {

        SceneEnvironment environment = scene.environment();

        Skybox skybox = SkyboxFactory.newSkyboxHDR(BerylFiles.getString("textures/skybox/hdr/sunrise_beach_2k.hdr"));

        environment.skybox(skybox);
    }

    public static Entity create(Scene scene, float x, float y, float z, float scale) {

        Entity water = scene.newEntity("Water");
        water.add(Transform.class).position(x, y, z).rotateX(radians(90)).scale(scale);

        WaterMeshView waterMeshView = new WaterMeshView(StaticMesh.quad(), getWaterMaterial());

        waterMeshView.clipPlane(0, 1, 0, water.get(Transform.class).position().y() + 0.1f);
        water.add(WaterMeshInstance.class).meshView(waterMeshView);
        water.add(Water.WaterController.class);

        scene.enhancedWater().setEnhancedWaterView(waterMeshView);

        return water;
    }

    private static WaterMaterial getWaterMaterial() {

        return WaterMaterial.getFactory().getMaterial("water", material -> {

            Texture2D dudv = GraphicsFactory.get().newTexture2D(BerylFiles.getString("textures/water/dudv.png"), PixelFormat.RGBA);
            Texture2D normalMap = GraphicsFactory.get().newTexture2D(BerylFiles.getString("textures/water/normalMap.png"), PixelFormat.RGBA);

            dudv.generateMipmaps();
            dudv.sampler().wrapMode(Sampler.WrapMode.REPEAT);
            dudv.sampler().minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
            dudv.sampler().magFilter(Sampler.MagFilter.LINEAR);
            dudv.sampler().maxAnisotropy(16);
            dudv.sampler().lodBias(0);

            normalMap.generateMipmaps();
            normalMap.sampler().wrapMode(Sampler.WrapMode.REPEAT);
            normalMap.sampler().minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
            normalMap.sampler().magFilter(Sampler.MagFilter.LINEAR);
            normalMap.sampler().maxAnisotropy(4);
            normalMap.sampler().lodBias(0);

            material.setDudvMap(dudv).setNormalMap(normalMap);

            material.tiling(3, 3)
                    .setColor(new Color(116/255.0f,204/255.0f,244/255.0f))
                    .setColorStrength(0.03f)
                    .setDistortionStrength(0.025f);
        });
    }
}
