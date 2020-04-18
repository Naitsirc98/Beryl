package naitsirc98.beryl.examples.app1;

import naitsirc98.beryl.audio.AudioClip;
import naitsirc98.beryl.audio.AudioDistanceModel;
import naitsirc98.beryl.audio.AudioSystem;
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
import naitsirc98.beryl.lights.LightRange;
import naitsirc98.beryl.lights.PointLight;
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
import naitsirc98.beryl.meshes.views.MeshView;
import naitsirc98.beryl.meshes.views.StaticMeshView;
import naitsirc98.beryl.meshes.views.WaterMeshView;
import naitsirc98.beryl.scenes.*;
import naitsirc98.beryl.scenes.components.audio.AudioPlayer;
import naitsirc98.beryl.scenes.components.behaviours.UpdateMutableBehaviour;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.MeshInstance;
import naitsirc98.beryl.scenes.components.meshes.StaticMeshInstance;
import naitsirc98.beryl.scenes.components.meshes.WaterMeshInstance;
import naitsirc98.beryl.util.Color;
import org.joml.Vector3f;
import org.lwjgl.openal.AL11;

import java.util.Random;

import static naitsirc98.beryl.scenes.EnhancedWaterUnit.ENHANCED_WATER_UNIT_0;
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

        StaticMesh quadMesh = StaticMeshLoader.get().load(BerylFiles.getPath("models/quad.obj")).loadedMesh(0).mesh();

        StaticMesh cubeMesh = StaticMeshLoader.get().load(BerylFiles.getPath("models/cube.obj")).loadedMesh(0).mesh();

        StaticMesh grassMesh = StaticMeshLoader.get().load(BerylFiles.getPath("models/grass.obj"),
                new StaticVertexHandler.Builder().normalFunction(n -> n.set(0, 1, 0)).build())
                .loadedMesh(0).mesh();

        Model<StaticMesh> treeModel = StaticMeshLoader.get()
                        .load("C:\\Users\\naits\\Downloads\\uploads_files_1970932_conifer_macedonian_pine(1)\\OBJ format\\conifer_macedonian_pine.obj",
                                new StaticVertexHandler.Builder().positionFunction(p -> p.mul(0.01f)).build());

        Log.trace(treeModel);

        Model<StaticMesh> lampModel = StaticMeshLoader.get()
                .load("C:\\Users\\naits\\Downloads\\uploads_files_1923232_2otdoorlightning\\lightning1.fbx",
                        new StaticVertexHandler.Builder().positionFunction(p -> p.mul(0.01f)).build());

        Log.trace(lampModel);

        Entity terrain = scene.newEntity();
        terrain.add(Transform.class).position(0, 0, 0).scale(1);
        terrain.add(StaticMeshInstance.class).meshView(new StaticMeshView(terrainMesh, getFloorMaterial()));

        Entity water = scene.newEntity();
        water.add(Transform.class).position(terrainSize / 2, -5.0f, terrainSize / 2).rotateX(radians(90)).scale(terrainSize / 2);

        WaterMeshView waterMeshView = new WaterMeshView(quadMesh, getWaterMaterial())
                .tiling(20)
                .waterColorStrength(0.03f)
                .distortionStrength(0.05f);

        waterMeshView.clipPlane(0, 1, 0, water.get(Transform.class).position().y() + 0.1f);
        water.add(WaterMeshInstance.class).meshView(waterMeshView);
        water.add(UpdateMutableBehaviour.class).onUpdate(self ->  {

            if(!self.exists("movement")) {
                self.set("movement", 0.0f);
            }

            float movement = self.get("movement");

            movement += 0.021f * Time.IDEAL_DELTA_TIME;
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

        for(int i = 0;i < 40;i++) {
            Entity grass = scene.newEntity();
            float x = RAND.nextInt((int) terrainSize);
            float z = RAND.nextInt((int) terrainSize);
            float y = terrainMesh.heightAt(0, 0, x, z);
            grass.get(Transform.class).position(x, y, z).scale(20.0f);
            grass.add(StaticMeshInstance.class).meshView(grassView);
        }

        StaticModelEntityFactory lampFactory = new StaticModelEntityFactory(lampModel);
        lampFactory.materialsFunction(this::lampMaterials);

        Entity lamp = lampFactory.newEntity("", scene);
        lamp.get(Transform.class).position(473.74f, 0.067f, 376.301f).scale(4);

        Camera camera = scene.camera();
        camera.lookAt(0, 0).position(391, 13.0f, 479);//.position(terrainSize / 2, 5, terrainSize / 2);
        // camera.farPlane(terrainSize);

        scene.environment().lights().pointLights().add(new PointLight()
                .position(471.379f, 4.051f, 375.764f)
                .color(Color.BLACK)
                .range(LightRange.MEDIUM));

        Entity cameraController = scene.newEntity();
        cameraController.add(CameraController.class);

        Entity forestSound = scene.newEntity();
        AudioPlayer forestAudioPlayer = forestSound.add(AudioPlayer.class);
        forestAudioPlayer.source()
                .minGain(0)
                .maxGain(1)
                .gain(1)
                .rollOff(1)
                .position(new Vector3f(terrainSize/2, 0, terrainSize/2))
                .looping(true);
        forestAudioPlayer.play(AudioClip.get("forest", params -> params.audioFile("G:\\__inglesjavi\\forest2.ogg")));

        Entity forestNightSound = scene.newEntity();
        AudioPlayer forestNightAudioPlayer = forestNightSound.add(AudioPlayer.class);
        forestNightAudioPlayer.source()
                .minGain(0)
                .maxGain(1)
                .gain(0)
                .rollOff(1)
                .position(new Vector3f(terrainSize/2, 0, terrainSize/2))
                .looping(true);
        forestNightAudioPlayer.play(AudioClip.get("forestNight", params -> params.audioFile("G:\\__inglesjavi\\forest_night.ogg")));

        SceneEnvironment environment = scene.environment();

        Entity skyboxController = scene.newEntity();
        skyboxController.add(UpdateMutableBehaviour.class).onUpdate(self -> {

            if(!self.exists("offset")) {
                self.set("offset", Time.minutes());
            }

            final float offset = self.get("offset");

            Skybox skybox = self.scene().environment().skybox();

            skybox.rotate(radians(0.0042f));

            final float time = Math.abs(sin((Time.minutes() - offset)*2));

            skybox.textureBlendFactor(time);

            DirectionalLight sun = self.scene().environment().lights().directionalLight();

            sun.color(Color.WHITE.intensify((1.0f - time)));

            forestAudioPlayer.source().gain(1.0f - time * 2);
            forestNightAudioPlayer.source().gain(time * time);

            scene.environment().fog().density(time / 2.5f);

            scene.environment().lights().pointLights().get(0).color(Color.WHITE.intensify(time * 1.25f));

            scene.environment().ambientColor(Color.WHITE.intensify(clamp(0.2f, 0.9f, 1.0f - time)));

            PhongMaterial lampMaterial = (PhongMaterial) lamp.get(StaticMeshInstance.class).meshView().material();
            lampMaterial.emissiveColor(Color.WHITE.intensify(clamp(0.2f, 2.0f, time * 1.2f)));
        });

        // AudioSystem.distanceModel(AudioDistanceModel.EXPONENT_DISTANCE);

        addWaterAudioSource(scene, terrainSize, 50, 445.163f, -5.879f, 319.965f);

        environment.skybox(new Skybox(BerylFiles.getString("textures/skybox/day"), BerylFiles.getString("textures/skybox/night")));
        environment.lights().directionalLight(new DirectionalLight().color(Color.WHITE.intensify(1.2f)).direction(-0.453f, -0.902f, 0.391f));
        environment.fog().density(DEFAULT_FOG_DENSITY);
    }

    private IMaterial lampMaterials(String meshName) {

        String dir = "C:\\Users\\naits\\Downloads\\uploads_files_1923232_2otdoorlightning\\textures\\unreal\\lightning1\\";

        return PhongMaterial.get("lamp", builder -> {

            Texture2D colorTexture = GraphicsFactory.get().newTexture2D(dir+"lightning1_BaseColor.tga", PixelFormat.RGBA);
            colorTexture.generateMipmaps();
            colorTexture.sampler().wrapMode(Sampler.WrapMode.REPEAT);
            colorTexture.sampler().minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
            colorTexture.sampler().magFilter(Sampler.MagFilter.LINEAR);
            colorTexture.sampler().lodBias(-1);

            Texture2D normalMap = GraphicsFactory.get().newTexture2D(dir+"lightning1_Normal.tga", PixelFormat.RGBA);
            normalMap.generateMipmaps();
            normalMap.sampler().wrapMode(Sampler.WrapMode.REPEAT);
            normalMap.sampler().minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
            normalMap.sampler().magFilter(Sampler.MagFilter.LINEAR);

            Texture2D emissiveMap = GraphicsFactory.get().newTexture2D(dir+"lightning1_Emissive.tga", PixelFormat.RGBA);
            emissiveMap.generateMipmaps();
            emissiveMap.sampler().wrapMode(Sampler.WrapMode.REPEAT);
            emissiveMap.sampler().minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
            emissiveMap.sampler().magFilter(Sampler.MagFilter.LINEAR);
            emissiveMap.sampler().lodBias(-1);

            builder.ambientMap(colorTexture).diffuseMap(colorTexture).normalMap(normalMap).emissiveMap(emissiveMap).emissiveColor(Color.WHITE);
        });
    }

    private void addWaterAudioSource(Scene scene, float terrainSize, float maxDistance, float x, float y, float z) {

        Entity waterSound = scene.newEntity();

        AudioPlayer waterAudioPlayer = waterSound.add(AudioPlayer.class);
        waterAudioPlayer.source()
                .gain(0.8f)
                .position(new Vector3f(x, y, z))
                .referenceDistance(1.0f)
                .maxDistance(maxDistance)
                .rollOff(0.6f)
                .looping(true);
        waterAudioPlayer.play(AudioClip.get("water", audioClipParams -> {
            audioClipParams.audioFile("G:\\__inglesjavi\\waterm.ogg");
        }));
    }

    private WaterMaterial getWaterMaterial() {
        return WaterMaterial.get("water", builder -> {

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

            builder.dudvMap(dudv).normalMap(normalMap);
        });
    }

    private IMaterial getGrassMaterial() {

        return PhongMaterial.get("grass", builder -> {

            Texture2D colorTexture = GraphicsFactory.get().newTexture2D(BerylFiles.getString("textures/grass.png"), PixelFormat.SRGBA);

            colorTexture.generateMipmaps();
            colorTexture.sampler().minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
            colorTexture.sampler().magFilter(Sampler.MagFilter.LINEAR);
            colorTexture.sampler().lodBias(0);

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
                        colorTexture.sampler().lodBias(0.1f);

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
                        colorTexture.sampler().lodBias(3.0f);

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
                        colorTexture.sampler().lodBias(-2.0f);

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
            colorMap.sampler().lodBias(-0.1f);
            builder.ambientMap(colorMap).diffuseMap(colorMap);
            builder.shininess(1);
            builder.textureCoordsFactor(50, 50);
        });
    }

    @Override
    protected void onUpdate() {

    }
}
