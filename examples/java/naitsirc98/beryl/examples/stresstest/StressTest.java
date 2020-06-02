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

public class StressTest extends BerylApplication {

    public StressTest() {
        BerylConfiguration.SHADOWS_ENABLED_ON_START.set(false);
        BerylConfiguration.SCENE_SHADING_MODEL.set(ShadingModel.PHONG);
        BerylConfiguration.GRAPHICS_MULTITHREADING_ENABLED.set(true);
        BerylConfiguration.FIRST_SCENE_NAME.set("Stress Test");
        BerylConfigurationHelper.developmentConfiguration();
    }

    @Override
    protected void onStart(Scene scene) {

        Entity cameraController = scene.newEntity();
        cameraController.add(CameraController.class);

        scene.camera().position(0, 100, -200);

        StaticMesh cubeMesh = StaticMesh.cube();

        Random rand = new Random();

        for(int i = 0;i < 50000;i++) {
            createEntity(scene, rand, cubeMesh, i);
        }

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

                    count.set(1000);

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
    }
}
