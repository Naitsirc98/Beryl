package naitsirc98.beryl.examples.space;

import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.core.BerylConfigurationHelper;
import naitsirc98.beryl.core.Time;
import naitsirc98.beryl.examples.common.CameraController;
import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.rendering.ShadingModel;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.lights.LightRange;
import naitsirc98.beryl.lights.PointLight;
import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.meshes.views.StaticMeshView;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.SceneManager;
import naitsirc98.beryl.scenes.components.behaviours.UpdateBehaviour;
import naitsirc98.beryl.scenes.components.behaviours.UpdateMutableBehaviour;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.StaticMeshInstance;
import naitsirc98.beryl.scenes.environment.skybox.Skybox;
import naitsirc98.beryl.scenes.environment.skybox.SkyboxFactory;
import naitsirc98.beryl.util.Color;
import org.joml.Vector2f;

public class SolarSystem extends BerylApplication {

    public SolarSystem() {
        BerylConfiguration.SHADOWS_ENABLED_ON_START.set(false);
        BerylConfiguration.SCENE_SHADING_MODEL.set(ShadingModel.PHONG);
        BerylConfiguration.FIRST_SCENE_NAME.set("Solar System");
        BerylConfigurationHelper.debugConfiguration();
    }

    @Override
    protected void onStart(Scene scene) {

        setPlanetsAndSun(scene);

        setEnvironment(scene);

        setCamera(scene);
    }

    private void setCamera(Scene scene) {
        Entity cameraController = scene.newEntity();
        cameraController.add(CameraController.class);
        scene.camera().position(90.476f, 1.485f, -1.656f).lookAt(700, 0);
    }

    private void setPlanetsAndSun(Scene scene) {

        StaticMesh sphere = StaticMesh.sphere();

        Entity earth = scene.newEntity("Earth");
        earth.add(Transform.class).position(0, 0, 30);
        earth.add(StaticMeshInstance.class).meshView(new StaticMeshView(sphere, getEarthMaterial()));
        earth.add(PlanetRotation.class).setRotationSpeed(1.0f);

        Entity sun = scene.newEntity("Sun");
        sun.add(Transform.class).position(0, 0, 1000).scale(3);
        sun.add(StaticMeshInstance.class).meshView(new StaticMeshView(sphere, getSunMaterial()));
        sun.add(PlanetRotation.class).setRotationSpeed(0.1f);

        scene.environment().lighting().pointLights()
                .add(new PointLight()
                        .color(Color.colorWhite().intensify(2))
                        .linear(0.0005f)
                        .quadratic(0.00000f)
                        .position(sun.get(Transform.class).position()));
    }

    private Material getSunMaterial() {
        return PhongMaterial.getFactory().getMaterial("Sun Material", material ->
                material.emissiveMap(loadTexture("D:\\Space Textures\\2k_sun.jpg"))).emissiveColor(Color.colorWhite());
    }

    private Material getEarthMaterial() {
        return PhongMaterial.getFactory().getMaterial("Earth material", material ->
                material.ambientMap(loadTexture("D:\\Space Textures\\2k_earth_night.jpg"))
                .diffuseMap(loadTexture("D:\\Space Textures\\2k_earth_day.jpg")))
                .specularMap(loadTexture("D:\\Space Textures\\2k_earth_specular_map.png"));
                // .normalMap(loadTexture("D:\\Space Textures\\2k_earth_normal_map.png"));
    }

    private void setEnvironment(Scene scene) {

        Skybox skybox = SkyboxFactory.newSkybox("C:\\Users\\naits\\Downloads\\skybox (2)");

        scene.environment().skybox(skybox);
    }

    private Texture2D loadTexture(String path) {
        return GraphicsFactory.get().newTexture2D(path, PixelFormat.RGBA);
    }

    private static class PlanetRotation extends UpdateBehaviour {

        private float rotationSpeed;

        @Override
        public void onUpdate() {
            get(Transform.class).rotateY(Time.time() * 0.25f * rotationSpeed);
        }

        public float getRotationSpeed() {
            return rotationSpeed;
        }

        public PlanetRotation setRotationSpeed(float rotationSpeed) {
            this.rotationSpeed = rotationSpeed;
            return this;
        }
    }
}
