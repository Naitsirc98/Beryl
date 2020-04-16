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
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.materials.WaterMaterial;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.meshes.TerrainMesh;
import naitsirc98.beryl.meshes.TerrainMeshLoader;
import naitsirc98.beryl.meshes.models.Model;
import naitsirc98.beryl.meshes.models.StaticMeshLoader;
import naitsirc98.beryl.meshes.models.StaticModelEntityFactory;
import naitsirc98.beryl.meshes.models.StaticVertexHandler;
import naitsirc98.beryl.meshes.views.StaticMeshView;
import naitsirc98.beryl.meshes.views.WaterMeshView;
import naitsirc98.beryl.scenes.*;
import naitsirc98.beryl.scenes.components.behaviours.UpdateMutableBehaviour;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.StaticMeshInstance;
import naitsirc98.beryl.scenes.components.meshes.WaterMeshInstance;
import naitsirc98.beryl.util.Color;

import java.util.Random;

import static naitsirc98.beryl.scenes.EnhancedWaterUnit.ENHANCED_WATER_UNIT_0;
import static naitsirc98.beryl.scenes.Fog.DEFAULT_FOG_DENSITY;
import static naitsirc98.beryl.scenes.SceneManager.newScene;
import static naitsirc98.beryl.util.Maths.radians;
import static naitsirc98.beryl.util.Maths.sin;


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

        StaticMesh quadMesh = StaticMeshLoader.get().load(BerylFiles.getPath("models/quad.obj")).loadedMesh(0).mesh();

        StaticMesh grassMesh = StaticMeshLoader.get().load(BerylFiles.getPath("models/grass.obj"),
                new StaticVertexHandler.Builder().normalFunction(n -> n.set(0, 1, 0)).build())
                .loadedMesh(0).mesh();

        Model<StaticMesh> treeModel = StaticMeshLoader.get()
                        .load("C:\\Users\\naits\\Downloads\\uploads_files_1970932_conifer_macedonian_pine(1)\\OBJ format\\conifer_macedonian_pine.obj",
                                new StaticVertexHandler.Builder().positionFunction(p -> p.mul(0.01f)).build());

        Log.trace(treeModel);

        Entity terrain = scene.newEntity();
        terrain.add(Transform.class).position(0, 0, 0).scale(1);
        terrain.add(StaticMeshInstance.class).meshView(new StaticMeshView(terrainMesh, getFloorMaterial()));

        Entity water = scene.newEntity();
        water.add(Transform.class).position(terrainSize/2, -6.0f, terrainSize/2).rotateX(radians(90)).scale(400);

        WaterMeshView waterMeshView = new WaterMeshView(quadMesh, getWaterMaterial())
                .tiling(20)
                .waterColorStrength(0.05f)
                .distortionStrength(0.03f);

        waterMeshView.clipPlane(0, 1, 0, water.get(Transform.class).position().y() + 0.2f);
        water.add(WaterMeshInstance.class).meshView(waterMeshView);
        water.add(UpdateMutableBehaviour.class).onUpdate(self ->  {

            if(!self.exists("movement")) {
                self.set("movement", 0.0f);
            }

            float movement = self.get("movement");

            movement += 0.02f * Time.IDEAL_DELTA_TIME;
            movement %= 1;

            self.get(WaterMeshInstance.class).meshView().texturesOffset(movement);

            self.set("movement", movement);
        });

        scene.enhancedWater().setEnhancedWaterView(ENHANCED_WATER_UNIT_0, waterMeshView);

        StaticModelEntityFactory treeFactory = new StaticModelEntityFactory(treeModel).materialsFunction(this::treeMaterialFunction);

        for(int i = 0;i < 400;i++) {

            Entity tree = treeFactory.newEntity(scene);

            float x;
            float z;
            float y;

            do {
                x =  RAND.nextInt((int) terrainSize);
                z =  RAND.nextInt((int) terrainSize);
                y =  terrainMesh.heightAt(0, 0, x, z);
            } while(y <= water.get(Transform.class).position().y() + 1);

            tree.get(Transform.class).position(x, y - 1, z);
        }

        StaticMeshView grassView = new StaticMeshView(grassMesh, getGrassMaterial());

        for(int i = 0;i < 100;i++) {
            Entity grass = scene.newEntity();
            float x = RAND.nextInt((int) terrainSize);
            float z = RAND.nextInt((int) terrainSize);
            float y = terrainMesh.heightAt(0, 0, x, z);
            grass.get(Transform.class).position(x, y, z).scale(6.0f);
            grass.add(StaticMeshInstance.class).meshView(grassView);
        }

        Camera camera = scene.camera();
        camera.lookAt(0, 0).position(268.543f, 10.0f, 526.378f);//.position(terrainSize / 2, 5, terrainSize / 2);

        Entity cameraController = scene.newEntity();
        cameraController.add(CameraController.class);

        SceneEnvironment environment = scene.environment();

        Entity skyboxController = scene.newEntity();
        skyboxController.add(UpdateMutableBehaviour.class).onUpdate(self -> {

            Skybox skybox = self.scene().environment().skybox();

            skybox.rotate(radians(0.004f));

            final float time = Math.abs(sin(Time.minutes() / 10));

            skybox.textureBlendFactor(time);
        });

        environment.skybox(new Skybox(BerylFiles.getString("textures/skybox/day"), BerylFiles.getString("textures/skybox/night")));
        environment.lights().directionalLight(new DirectionalLight().color(Color.WHITE).direction(-0.23f, -0.5f, 0.34f));
        environment.ambientColor(new Color(0.8f, 0.8f, 0.8f));
        environment.fog().density(DEFAULT_FOG_DENSITY * 2);
    }

    private WaterMaterial getWaterMaterial() {
        return WaterMaterial.get("water", builder -> {

            Texture2D dudv = GraphicsFactory.get().newTexture2D(BerylFiles.getString("textures/water/dudv.png"), PixelFormat.RGBA);
            Texture2D normalMap = GraphicsFactory.get().newTexture2D(BerylFiles.getString("textures/water/normalMap.png"), PixelFormat.RGBA);


            dudv.generateMipmaps();
            dudv.sampler().wrapMode(Sampler.WrapMode.REPEAT);
            dudv.sampler().minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
            dudv.sampler().magFilter(Sampler.MagFilter.LINEAR);
            dudv.sampler().maxAnisotropy(4);
            dudv.sampler().lodBias(0);

            normalMap.generateMipmaps();
            normalMap.sampler().wrapMode(Sampler.WrapMode.REPEAT);
            normalMap.sampler().minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
            normalMap.sampler().magFilter(Sampler.MagFilter.LINEAR);
            normalMap.sampler().maxAnisotropy(4);
            normalMap.sampler().lodBias(0);

            builder.dudvMap(dudv).normalMap(normalMap);
        });
    }

    private IMaterial getGrassMaterial() {

        return PhongMaterial.get("grass", builder -> {

            Texture2D colorTexture = GraphicsFactory.get().newTexture2D(BerylFiles.getString("textures/grass.png"), PixelFormat.SRGBA);

            colorTexture.generateMipmaps();
            colorTexture.sampler().minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
            colorTexture.sampler().magFilter(Sampler.MagFilter.LINEAR);
            colorTexture.sampler().lodBias(1);

            builder.ambientMap(colorTexture).diffuseMap(colorTexture);

        });
    }

    private PhongMaterial treeMaterialFunction(String meshName) {

        switch(meshName) {

            case "conifer_macedonian_pine_5":
                return PhongMaterial.get("trunk", builder -> {

                    try(Image image = ImageFactory
                            .newImage("C:\\Users\\naits\\Downloads\\uploads_files_1970932_conifer_macedonian_pine(1)\\OBJ format\\Bark_Color.png",
                                    PixelFormat.SRGBA)) {

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
                                    PixelFormat.SRGBA)) {

                        Texture2D colorTexture = GraphicsFactory.get().newTexture2D();

                        colorTexture.pixels(image);

                        colorTexture.generateMipmaps();

                        colorTexture.sampler().minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
                        colorTexture.sampler().magFilter(Sampler.MagFilter.LINEAR);
                        colorTexture.sampler().lodBias(-1.8f);

                        builder.ambientMap(colorTexture).diffuseMap(colorTexture);
                    }

                });

            case "/Game/conifer_macedonian_pine_Leaf_Mat_conifer_macedonian_pine_Leaf_Mat":

                return PhongMaterial.get("leaf", builder -> {

                    try(Image image = ImageFactory
                            .newImage("C:\\Users\\naits\\Downloads\\uploads_files_1970932_conifer_macedonian_pine(1)\\OBJ format\\conifer_macedonian_pine_Color.png",
                                    PixelFormat.SRGBA)) {

                        Texture2D colorTexture = GraphicsFactory.get().newTexture2D();

                        colorTexture.pixels(image);

                        colorTexture.generateMipmaps();

                        colorTexture.sampler().minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
                        colorTexture.sampler().magFilter(Sampler.MagFilter.LINEAR);
                        colorTexture.sampler().lodBias(-1.8f);

                        builder.ambientMap(colorTexture).diffuseMap(colorTexture);
                    }

                });
        }

        return PhongMaterial.getDefault();
    }

    private PhongMaterial getFloorMaterial() {
        return PhongMaterial.get("floor", builder -> {
            Texture2D colorMap = GraphicsFactory.get()
                    .newTexture2D("C:\\Users\\naits\\Downloads\\TexturesCom_Grass0157_1_seamless_S.jpg", PixelFormat.SRGBA);
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
}
