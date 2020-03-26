package naitsirc98.beryl.examples.app1;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.GraphicsAPI;
import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.rendering.RenderingPaths;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.images.Image;
import naitsirc98.beryl.images.ImageFactory;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.lights.LightRange;
import naitsirc98.beryl.lights.PointLight;
import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.meshes.models.AssimpModelLoader;
import naitsirc98.beryl.meshes.models.Model;
import naitsirc98.beryl.resources.ResourceManager;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.behaviours.UpdateMutableBehaviour;
import naitsirc98.beryl.scenes.components.camera.Camera;
import naitsirc98.beryl.scenes.components.lights.LightSource;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.MeshView;
import naitsirc98.beryl.util.Color;
import org.joml.Vector3f;

import java.util.Random;

import static naitsirc98.beryl.graphics.rendering.RenderingPaths.RPATH_PHONG;
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
        // BerylConfiguration.INITIAL_TIME_VALUE.set(4000.0);
        // BerylConfiguration.WINDOW_RESIZABLE.set(false);
        BerylConfiguration.SHOW_DEBUG_INFO.set(true);
        BerylConfiguration.GRAPHICS_API.set(GraphicsAPI.OPENGL);
        BerylConfiguration.VULKAN_ENABLE_DEBUG_MESSAGES.set(true);
        BerylConfiguration.VULKAN_ENABLE_VALIDATION_LAYERS.set(false);
        // BerylConfiguration.WINDOW_DISPLAY_MODE.set(DisplayMode.FULLSCREEN);
        // BerylConfiguration.VSYNC.set(true);
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

        Scene scene = newScene();

        Texture2D diffuseTexture = GraphicsFactory.get().newTexture2D();

        ResourceManager.track(diffuseTexture);

        try(Image image = ImageFactory.newImage("C:\\Users\\naits\\Desktop\\milo_raro.jpeg", PixelFormat.RGBA)) {
            diffuseTexture.pixels(1, image);
        }

        PhongMaterial material = PhongMaterial.get("MyMaterial", builder ->
                builder.ambientMap(diffuseTexture)
                .diffuseMap(diffuseTexture));

        AssimpModelLoader modelLoader = new AssimpModelLoader();

        Mesh cubeMesh = new Mesh(modelLoader.load(BerylFiles.getPath("models/cube.obj")).mesh(0).vertexData(), PhongMaterial.getDefault());
        Mesh quadMesh = new Mesh(modelLoader.load(BerylFiles.getPath("models/quad.obj")).mesh(0).vertexData(), PhongMaterial.getDefault());
        Mesh sphereMesh = new Mesh(modelLoader.load(BerylFiles.getPath("models/sphere.obj")).mesh(0).vertexData(), PhongMaterial.getDefault());

        Model model = new AssimpModelLoader().load(
                // BerylFiles.getPath("models/chalet.obj"));
                ("C:\\Users\\naits\\Downloads\\Cerberus_by_Andrew_Maximov\\Cerberus_by_Andrew_Maximov\\Cerberus_LP.FBX"));

        model.forEach(node -> {

            System.out.println(">>>" + node.name());

            System.out.println("    >> Meshes: ");
            for(int i = 0;i < node.meshCount();i++) {
                System.out.println("    " + node.mesh(i).name());
            }

            if(node.childCount() > 0) {

                System.out.println("    >> Children:");
                for(int i = 0;i < node.childCount();i++) {
                    System.out.println("    " + node.child(i));
                }
            }

            System.out.println();
        });

        Texture2D modelTexture = GraphicsFactory.get().newTexture2D();

        try(Image image = ImageFactory.newImage(BerylFiles.getString("textures/chalet.jpg"), PixelFormat.RGBA)) {
            modelTexture.pixels(1, image);
        }

        Mesh modelMesh = new Mesh(model.mesh(0).vertexData(), PhongMaterial.get("MODEL",
                builder -> builder.emissiveMap(modelTexture).emissiveColor(Color.WHITE)));

        for(int i = 0;i < 10000;i++) {

            final float angle = RAND.nextFloat();

            PhongMaterial mat = PhongMaterial.get(i +"",
                    builder -> builder.color(new Color(RAND.nextFloat(), RAND.nextFloat(), RAND.nextFloat())));

            Entity entity = scene.newEntity();
            entity.add(Transform.class).scale(0.25f).rotate(radians(-90), 1, 0, 0).position(RAND.nextInt(500), -RAND.nextInt(500), -RAND.nextInt(500));
            entity.add(MeshView.class).mesh(sphereMesh).material(mat);
            entity.add(UpdateMutableBehaviour.class).onUpdate(thisBehaviour -> {
                Transform transform = thisBehaviour.get(Transform.class);
                transform.rotateY(radians(angle));
                // thisBehaviour.entity().destroy();
                addOrRemoveRandomly(thisBehaviour.entity(), sphereMesh, mat);
            });
        }


        Entity camera = scene.newEntity("Camera");
        camera.add(Transform.class).position(100, 0, 300);
        camera.add(Camera.class).lookAt(0, 0).renderingPath(RenderingPaths.get(RPATH_PHONG));
        camera.add(CameraController.class);



        Entity light = scene.newEntity("Light");
        light.add(LightSource.class).light(new PointLight()
                .color(new Color(1, 1, 1, 1))
                .position(new Vector3f(0.0f))
                .range(LightRange.MEDIUM));
        light.add(Transform.class).position(0, 0, 0);
        light.add(MeshView.class).mesh(cubeMesh).material(
                PhongMaterial.get("LightMaterial", builder -> builder.emissiveColor(Color.WHITE)));
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
            model.add(MeshView.class).mesh(mesh).material(material);
            model.add(UpdateMutableBehaviour.class).onUpdate(thisBehaviour -> {
                Transform transform = thisBehaviour.get(Transform.class);
                transform.rotateY(radians(angle));
                addOrRemoveRandomly(thisBehaviour.entity(), mesh, material);
            });
        }
    }
}
