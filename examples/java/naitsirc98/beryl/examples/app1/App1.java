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
import naitsirc98.beryl.lights.DirectionalLight;
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
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.MeshView;
import naitsirc98.beryl.util.Color;
import org.joml.Vector3f;

import java.util.Random;

import static naitsirc98.beryl.scenes.SceneManager.newScene;
import static naitsirc98.beryl.util.Maths.radians;


public class App1 extends BerylApplication {

    private static final Random RAND = new Random(System.nanoTime());

    public static Mesh cubeMesh;
    public static Mesh sphereMesh;
    public static Mesh quadMesh;

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

        cubeMesh = new Mesh(modelLoader.load(BerylFiles.getPath("models/cube.obj")).mesh(0).vertexData(), PhongMaterial.getDefault());
        quadMesh = new Mesh(modelLoader.load(BerylFiles.getPath("models/quad.obj")).mesh(0).vertexData(), PhongMaterial.getDefault());
        sphereMesh = new Mesh(modelLoader.load(BerylFiles.getPath("models/sphere.obj")).mesh(0).vertexData(), PhongMaterial.getDefault());

        Model model = new AssimpModelLoader().load(
                ("C:\\Users\\naits\\Downloads\\87xm06x9pyps-room\\OBJ\\Room.obj"));
                // BerylFiles.getPath("models/chalet.obj"));
                // ("C:\\Users\\naits\\Downloads\\Cerberus_by_Andrew_Maximov\\Cerberus_by_Andrew_Maximov\\Cerberus_LP.FBX"));

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

        for(int i = 0;i < model.meshCount();i++) {

            final float angle = RAND.nextFloat();

            Entity entity = scene.newEntity();
            entity.add(Transform.class).scale(0.5f);//.scale(0.25f).rotate(radians(-90), 1, 0, 0).position(RAND.nextInt(500), -RAND.nextInt(500), -RAND.nextInt(500));

            Mesh modelMesh = new Mesh(model.mesh(i).vertexData(), PhongMaterial.get("MODEL",
                    builder -> builder.emissiveMap(modelTexture).emissiveColor(Color.WHITE)));

            PhongMaterial mat;

            if (model.mesh(i).name().equals("lamp_legup_glass1_Cap_16")) {
                mat = PhongMaterial.get(i + "", builder -> builder.emissiveColor(Color.WHITE));
                /*
                entity.add(LightSource.class).light(new SpotLight()
                        .range(LightRange.SMALL)
                        .position(new Vector3f(42.608f, 1.682f, -40.58f))
                        .direction(new Vector3f(-0.072f, -0.995f, -0.071f)));

                 */
            } else {
                mat = PhongMaterial.get(i + "", builder -> builder.color(Color.WHITE));
                // builder -> builder.color(new Color(RAND.nextFloat(), RAND.nextFloat(), RAND.nextFloat())));
            }
            // TODO: uncomment
            entity.add(MeshView.class).mesh(modelMesh).material(mat);
            entity.add(UpdateMutableBehaviour.class).onUpdate(thisBehaviour -> {
                Transform transform = thisBehaviour.get(Transform.class);
                // transform.rotateY(radians(angle));
                // thisBehaviour.entity().destroy();
                // addOrRemoveRandomly(thisBehaviour.entity(), sphereMesh, mat);
            });
        }

        Entity camera = scene.newEntity("Camera");
        // camera.add(Transform.class).position(100, 0, 300);
        camera.add(Transform.class).position(0, 0, -3);
        camera.add(Camera.class).lookAt(0, 0).renderingPath(RenderingPaths.get(100)).clearColor(new Color(0.1f, 0.1f, 0.1f));
        camera.add(CameraController.class);

        Entity sun = scene.newEntity("Sun");
        sun.add(Transform.class).position(-3042.442f, 925.903f, 187.437f);
        sun.add(MeshView.class).mesh(sphereMesh).material(PhongMaterial.get("SUN",
                builder -> builder.emissiveColor(new Color(1f, 0.976f, 0.501f))));

        scene.environment().directionalLight(new DirectionalLight()
                .color(new Color(0.3f, 0.3f, 0.3f))
                .direction(new Vector3f(0.954f, -0.292f, -0.07f)));
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
