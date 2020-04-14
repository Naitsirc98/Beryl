package naitsirc98.beryl.examples.app1;

import naitsirc98.beryl.core.*;
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
import naitsirc98.beryl.materials.IMaterial;
import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.meshes.MeshView;
import naitsirc98.beryl.meshes.TerrainMesh;
import naitsirc98.beryl.meshes.TerrainMeshLoader;
import naitsirc98.beryl.meshes.models.Model;
import naitsirc98.beryl.meshes.models.ModelEntityFactory;
import naitsirc98.beryl.meshes.models.StaticMeshLoader;
import naitsirc98.beryl.meshes.models.StaticVertexHandler;
import naitsirc98.beryl.scenes.*;
import naitsirc98.beryl.scenes.components.behaviours.UpdateMutableBehaviour;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.MeshInstance;
import naitsirc98.beryl.util.Color;

import java.util.Random;

import static naitsirc98.beryl.scenes.Fog.DEFAULT_FOG_DENSITY;
import static naitsirc98.beryl.scenes.SceneManager.newScene;
import static naitsirc98.beryl.util.Maths.*;


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

        float terrainSize = 800;

        TerrainMesh terrainMesh = TerrainMeshLoader.get().load("Terrain", BerylFiles.getString("textures/terrain_heightmap.png"), terrainSize);

        Mesh grassMesh = StaticMeshLoader.get().load(BerylFiles.getPath("models/grass.obj"),
                new StaticVertexHandler.Builder().normalFunction(n -> n.set(0, 1, 0)).build())
                .loadedMesh(0).mesh();

        Model treeModel = StaticMeshLoader.get()
                        .load("C:\\Users\\naits\\Downloads\\uploads_files_1970932_conifer_macedonian_pine(1)\\OBJ format\\conifer_macedonian_pine.obj",
                                new StaticVertexHandler.Builder().positionFunction(p -> p.mul(0.01f)).build());

        Log.trace(treeModel);

        Entity terrain = scene.newEntity();
        terrain.add(Transform.class).position(0, 0, 0).scale(1);
        terrain.add(MeshInstance.class).meshView(new MeshView(terrainMesh, getFloorMaterial()));

        ModelEntityFactory treeFactory = new ModelEntityFactory(treeModel).materialsFunction(this::treeMaterialFunction);

        for(int i = 0;i < 1000;i++) {
            Entity tree = treeFactory.newEntity(scene);
            float x = RAND.nextInt((int) terrainSize);
            float z = RAND.nextInt((int) terrainSize);
            float y = terrainMesh.heightAt(0, 0, x, z);
            tree.get(Transform.class).position(x, y, z);
        }

        MeshView grassView = new MeshView(grassMesh, getGrassMaterial());

        for(int i = 0;i < 1000;i++) {
            Entity grass = scene.newEntity();
            float x = RAND.nextInt((int) terrainSize);
            float z = RAND.nextInt((int) terrainSize);
            float y = terrainMesh.heightAt(0, 0, x, z);
            grass.get(Transform.class).position(x, y, z).scale(2.0f);
            grass.add(MeshInstance.class).meshView(grassView);
        }

        Camera camera = scene.camera();
        camera.lookAt(0, 0).position(terrainSize / 2, 5, terrainSize / 2);

        Entity cameraController = scene.newEntity();
        cameraController.add(CameraController.class);

        SceneEnvironment environment = scene.environment();

        Entity skyboxController = scene.newEntity();
        skyboxController.add(UpdateMutableBehaviour.class).onUpdate(self -> {

            Skybox skybox = self.scene().environment().skybox();

            skybox.rotate(radians(0.004f));

            final float time = Math.abs(sin(Time.minutes()));

            skybox.textureBlendFactor(time);
        });

        environment.skybox(new Skybox(BerylFiles.getString("textures/skybox/day"), BerylFiles.getString("textures/skybox/night")));
        environment.lights().directionalLight(new DirectionalLight().color(Color.WHITE).direction(-1, -1, 0));
        environment.ambientColor(new Color(0.8f, 0.8f, 0.8f));
        environment.fog().density(DEFAULT_FOG_DENSITY);
    }

    private IMaterial getGrassMaterial() {

        return PhongMaterial.get("grass", builder -> {

            Texture2D colorTexture = GraphicsFactory.get().newTexture2D(BerylFiles.getString("textures/grass.png"), PixelFormat.RGBA);

            colorTexture.generateMipmaps();
            colorTexture.sampler().minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
            colorTexture.sampler().magFilter(Sampler.MagFilter.LINEAR);
            colorTexture.sampler().lodBias(-4);

            builder.ambientMap(colorTexture).diffuseMap(colorTexture);

        });
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

                        colorTexture.generateMipmaps();

                        colorTexture.sampler().minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
                        colorTexture.sampler().magFilter(Sampler.MagFilter.LINEAR);

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

                        colorTexture.generateMipmaps();

                        colorTexture.sampler().minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
                        colorTexture.sampler().magFilter(Sampler.MagFilter.LINEAR);
                        colorTexture.sampler().lodBias(-2.5f);

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

                        colorTexture.generateMipmaps();

                        colorTexture.sampler().minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
                        colorTexture.sampler().magFilter(Sampler.MagFilter.LINEAR);
                        colorTexture.sampler().lodBias(-2.5f);

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
            colorMap.sampler().maxAnisotropy(16);
            colorMap.generateMipmaps();
            colorMap.sampler().wrapMode(Sampler.WrapMode.REPEAT);
            colorMap.sampler().magFilter(Sampler.MagFilter.LINEAR);
            colorMap.sampler().minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
            colorMap.sampler().lodBias(0);
            builder.ambientMap(colorMap).diffuseMap(colorMap);
            builder.shininess(1);
            builder.textureCoordsFactor(250, 250);
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
