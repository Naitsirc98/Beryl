package naitsirc98.beryl.examples.app1;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.GraphicsAPI;
import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Sampler;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.images.Image;
import naitsirc98.beryl.images.ImageFactory;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.meshes.MeshView;
import naitsirc98.beryl.meshes.models.Model;
import naitsirc98.beryl.meshes.models.ModelEntityFactory;
import naitsirc98.beryl.meshes.models.StaticMeshLoader;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.SceneEnvironment;
import naitsirc98.beryl.scenes.components.behaviours.UpdateMutableBehaviour;
import naitsirc98.beryl.scenes.components.camera.Camera;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.MeshInstance;
import naitsirc98.beryl.util.Color;

import java.util.Random;

import static naitsirc98.beryl.scenes.SceneManager.newScene;
import static naitsirc98.beryl.util.Maths.radians;


public class App1 extends BerylApplication {

    private static final Random RAND = new Random(System.nanoTime());

    public static void main(String[] args) {

        BerylConfiguration.SET_CONFIGURATION_METHOD.set(App1::setConfiguration);

        Beryl.launch(new App1());
    }

    private static void setConfiguration() {
        BerylConfiguration.DEBUG.set(true);
        BerylConfiguration.INTERNAL_DEBUG.set(true);
        BerylConfiguration.SHOW_DEBUG_INFO.set(true);
        BerylConfiguration.GRAPHICS_API.set(GraphicsAPI.OPENGL);
    }

    private App1() {

    }

    @Override
    protected void onStart() {

        Window.get().center();

        int count = 1; // RAND.nextInt(1) + 2;

        for(int i = 0;i < count;i++) {
            addScene();
        }

    }

    private void addScene() {

        Scene scene = newScene("Scene");

        StaticMeshLoader modelLoader = new StaticMeshLoader();

        Mesh cubeMesh = modelLoader.load(BerylFiles.getPath("models/cube.obj")).loadedMesh(0).mesh();


        Model treeModel = modelLoader
                        .load("C:\\Users\\naits\\Downloads\\uploads_files_1970932_conifer_macedonian_pine(1)\\OBJ format\\conifer_macedonian_pine.obj");

        Log.trace(treeModel);

        Entity floor = scene.newEntity("floor");
        floor.add(Transform.class).position(0, -0.1f, 0).scale(1000, 0.01f, 1000);
        floor.add(MeshInstance.class).meshView(new MeshView(cubeMesh, getFloorMaterial()));

        ModelEntityFactory treeFactory = new ModelEntityFactory(treeModel).materialsFunction(this::treeMaterialFunction);

        for(int i = 0;i < 1000;i++) {
            Entity tree = treeFactory.newEntity(scene);
            tree.get(Transform.class).position(RAND.nextInt(500), 0, RAND.nextInt(500)).scale(0.01f);
            if (i == 0) {
                tree.get(Transform.class).position(0, 0, 0);
            }
        }

        Entity camera = scene.newEntity("Camera");
        camera.add(Transform.class).position(0, 0, 3);
        camera.add(Camera.class).lookAt(0, 0).clearColor(new Color(0.3f, 0.3f, 0.3f));
        camera.add(CameraController.class);

        SceneEnvironment environment = scene.environment();

        environment.directionalLight(new DirectionalLight().color(Color.WHITE).direction(1, 1, 0));
        environment.ambientColor(new Color(0.8f, 0.8f, 0.8f));
    }

    private PhongMaterial treeMaterialFunction(String meshName) {

        switch(meshName) {

            case "conifer_macedonian_pine_5":
                return PhongMaterial.get("trunk", builder -> {

                    try(Image image = ImageFactory
                            .newImage("C:\\Users\\naits\\Downloads\\uploads_files_1970932_conifer_macedonian_pine(1)\\OBJ format\\Bark_Color.png",
                                    PixelFormat.RGBA)) {

                        Texture2D colorTexture = GraphicsFactory.get().newTexture2D();

                        colorTexture.pixels(image);

                        colorTexture.sampler().minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
                        colorTexture.sampler().magFilter(Sampler.MagFilter.LINEAR);

                        colorTexture.generateMipmaps();

                        builder.ambientMap(colorTexture).diffuseMap(colorTexture);
                    }

                });
            case "/Game/Cap_Branch_Mat_Cap_Branch_Mat":

                return PhongMaterial.get("cap", builder -> {

                    try(Image image = ImageFactory
                            .newImage("C:\\Users\\naits\\Downloads\\uploads_files_1970932_conifer_macedonian_pine(1)\\OBJ format\\Cap_Color.png",
                                    PixelFormat.RGBA)) {

                        Texture2D colorTexture = GraphicsFactory.get().newTexture2D();

                        colorTexture.pixels(image);

                        colorTexture.sampler().minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
                        colorTexture.sampler().magFilter(Sampler.MagFilter.LINEAR);

                        colorTexture.generateMipmaps();

                        builder.ambientMap(colorTexture).diffuseMap(colorTexture);
                    }

                });

            case "/Game/conifer_macedonian_pine_Leaf_Mat_conifer_macedonian_pine_Leaf_Mat":

                return PhongMaterial.get("leaf", builder -> {

                    try(Image image = ImageFactory
                            .newImage("C:\\Users\\naits\\Downloads\\uploads_files_1970932_conifer_macedonian_pine(1)\\OBJ format\\conifer_macedonian_pine_Color.png",
                                    PixelFormat.RGBA)) {

                        Texture2D colorTexture = GraphicsFactory.get().newTexture2D();

                        colorTexture.pixels(image);

                        colorTexture.sampler().minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
                        colorTexture.sampler().magFilter(Sampler.MagFilter.LINEAR);

                        colorTexture.generateMipmaps();

                        builder.ambientMap(colorTexture).diffuseMap(colorTexture);
                    }

                });
        }

        return PhongMaterial.getDefault();
    }

    private PhongMaterial getFloorMaterial() {
        return PhongMaterial.get("floor", builder -> {
            Texture2D colorMap = GraphicsFactory.get()
                    .newTexture2D("C:\\Users\\naits\\Downloads\\TexturesCom_Grass0157_1_seamless_S.jpg", PixelFormat.RGBA);
            colorMap.sampler().wrapMode(Sampler.WrapMode.REPEAT);
            colorMap.sampler().maxAnisotropy(16);
            colorMap.generateMipmaps();
            builder.ambientMap(colorMap).diffuseMap(colorMap);
            builder.shininess(1);
            builder.textureCoordsFactor(1000, 1000);
        });
    }

    @Override
    protected void onUpdate() {

    }

    private void addOrRemoveRandomly(Entity entity, Mesh mesh, Material material) {

        if(RAND.nextFloat() < 0.001f) {

            entity.destroy();

            final float angle = RAND.nextFloat();

            Entity model = entity.scene().newEntity();
            model.add(Transform.class).scale(0.25f).position(RAND.nextInt(500), -RAND.nextInt(500), -RAND.nextInt(500));
            model.add(MeshInstance.class).meshView(new MeshView(mesh, material));
            model.add(UpdateMutableBehaviour.class).onUpdate(thisBehaviour -> {
                Transform transform = thisBehaviour.get(Transform.class);
                transform.rotateY(radians(angle));
                addOrRemoveRandomly(thisBehaviour.entity(), mesh, material);
            });
        }
    }
}
