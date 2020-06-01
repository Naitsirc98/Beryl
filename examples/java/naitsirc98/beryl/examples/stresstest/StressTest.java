package naitsirc98.beryl.examples.stresstest;

import naitsirc98.beryl.core.*;
import naitsirc98.beryl.examples.common.CameraController;
import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.rendering.ShadingModel;
import naitsirc98.beryl.graphics.textures.Sampler;
import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.meshes.views.StaticMeshView;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.SceneManager;
import naitsirc98.beryl.scenes.components.behaviours.LateMutableBehaviour;
import naitsirc98.beryl.scenes.components.behaviours.UpdateMutableBehaviour;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.StaticMeshInstance;
import naitsirc98.beryl.scenes.environment.skybox.SkyboxFactory;
import naitsirc98.beryl.tasks.Task;
import naitsirc98.beryl.tasks.TaskManager;
import naitsirc98.beryl.util.Color;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class StressTest extends BerylApplication {

    private List<String> imageList;
    private Map<String, Texture2D> textureCache;

    public StressTest() {
        BerylConfiguration.SHADOWS_ENABLED_ON_START.set(false);
        BerylConfiguration.SCENE_SHADING_MODEL.set(ShadingModel.PHONG);
        BerylConfiguration.GRAPHICS_MULTITHREADING_ENABLED.set(true);
        BerylConfiguration.FIRST_SCENE_NAME.set("Stress Test");
        BerylConfigurationHelper.developmentConfiguration();

        try {
            imageList = Files.list(Paths.get("C:\\Users\\naits\\Downloads\\cats-master\\cat_photos"))
                    .map(Path::toFile).map(File::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        textureCache = new WeakHashMap<>();
    }

    @Override
    protected void onStart(Scene scene) {

        Entity cameraController = scene.newEntity();
        cameraController.add(CameraController.class);

        scene.camera().position(0, 100, -200);

        StaticMesh cubeMesh = StaticMesh.cube();

        Random rand = new Random();

        for(int i = 0;i < 20000;i++) {
            createEntity(scene, rand, cubeMesh, i);
        }

        Log.info("Loaded " + textureCache.size() + " textures");

        // textureCache.clear();

        scene.environment().skybox(SkyboxFactory.newSkybox(BerylFiles.getString("textures/skybox/day")));

        scene.environment().lighting().directionalLight(new DirectionalLight().direction(0, 0, -1));

        createAndDestroyObjects(scene, rand, cubeMesh);
    }

    private void createAndDestroyObjects(Scene scene, Random rand, StaticMesh cubeMesh) {

        AtomicReference<Float> time = new AtomicReference<>(0.0f);

        AtomicBoolean destroying = new AtomicBoolean(true);
        AtomicInteger count = new AtomicInteger();

        Entity controller = scene.newEntity();

        controller.add(LateMutableBehaviour.class).onLateUpdate(self -> {

            if(Time.seconds() - time.get() >= 0.1f) {

                if(destroying.get()) {

                    count.set(rand.nextInt(5000));

                    int n = count.get();

                    for(int i = 0;i < n;i++) {
                        scene.entity(i + "").destroy();
                    }

                    destroying.set(false);

                } else {

                    int n = count.getAndSet(-1);

                    TaskManager.submitGraphicsTask(new Task() {
                        @Override
                        protected void perform() {

                            for(int i = 0;i < n;i++) {

                                if(scene.exists(i + "")) {
                                    break;
                                }

                                createEntity(scene, rand, cubeMesh, i);

                                destroying.set(true);
                            }
                        }
                    });
                }

                time.set(Time.seconds());
            }
        });

        // controller.disable();
    }

    private void createEntity(Scene scene, Random rand, StaticMesh cubeMesh, int index) {

        Entity cube = scene.newEntity(index + "");

        cube.add(Transform.class).position(rand.nextInt(300), rand.nextInt(300), rand.nextInt(300));

        StaticMeshView meshView = new StaticMeshView(cubeMesh, createRandomMaterial(rand));

        cube.add(StaticMeshInstance.class).meshView(meshView);

        cube.add(UpdateMutableBehaviour.class).onUpdate(self -> self.get(Transform.class).rotateY(Time.seconds()));
    }

    private Material createRandomMaterial(Random rand) {
        return PhongMaterial.getFactory().getMaterial(rand.nextInt(10000) + "material", material -> {
           material.color(Color.colorRandom());
        });
        /*
        return PBRMetallicMaterial.getFactory().getMaterial(rand.nextInt() + "", material -> {
            // material.albedoMap(getRandomTexture(rand));
            material.albedo(new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
            material.roughness(rand.nextFloat());
            material.metallic(rand.nextFloat());
            material.occlusion(rand.nextFloat());
            // material.colorMap(getRandomTexture(rand));
            // material.color(new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
        });

         */
    }

    private Texture2D getRandomTexture(Random rand) {

        final int index = rand.nextInt(imageList.size());

        String file = imageList.get(index);

        if(textureCache.containsKey(file)) {
            return textureCache.get(file);
        }

        Texture2D texture = GraphicsFactory.get().newTexture2D(file, PixelFormat.RGBA);

        texture.setQuality(Texture.Quality.MEDIUM);
        texture.sampler().wrapMode(Sampler.WrapMode.REPEAT);

        textureCache.put(file, texture);

        return texture;
    }
}
