package naitsirc98.beryl.examples.app1;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.core.BerylConfiguration;
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
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.SceneManager;
import naitsirc98.beryl.scenes.components.behaviours.UpdateMutableBehaviour;
import naitsirc98.beryl.scenes.components.camera.Camera;
import naitsirc98.beryl.scenes.components.lights.LightSource;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.MeshView;
import naitsirc98.beryl.util.Color;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.Random;

import static naitsirc98.beryl.graphics.rendering.RenderingPaths.RPATH_PHONG;
import static naitsirc98.beryl.meshes.vertices.VertexLayout.VERTEX_LAYOUT_3D;
import static naitsirc98.beryl.util.Maths.radians;
import static naitsirc98.beryl.util.types.DataType.FLOAT32;


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
        BerylConfiguration.GRAPHICS_API.set(GraphicsAPI.VULKAN);
        BerylConfiguration.VULKAN_ENABLE_DEBUG_MESSAGES.set(false);
        BerylConfiguration.VULKAN_ENABLE_VALIDATION_LAYERS.set(false);
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

        Scene scene = new Scene();

        Texture2D diffuseTexture = GraphicsFactory.get().newTexture2D();

        try(Image image = ImageFactory.newImage("C:\\Users\\naits\\Desktop\\milo_raro.jpeg", true, PixelFormat.RGBA)) {
            diffuseTexture.pixels(1, image);
        }

        PhongMaterial material = new PhongMaterial.Builder("MyMaterial")
                // .diffuseColor(Color.GREEN);
                .ambientMap(diffuseTexture)
                .diffuseMap(diffuseTexture)
                .build();

        Mesh mesh = new Mesh(VertexData.builder(VERTEX_LAYOUT_3D).vertices(getCubeVertices()).build(), material);

        for(int i = 0;i < 10000;i++) {

            final float angle = RAND.nextFloat();

            Entity model = scene.newEntity();
            model.add(Transform.class).position(RAND.nextInt(200), -RAND.nextInt(200), -RAND.nextInt(200));
            model.add(MeshView.class).mesh(mesh);
            model.add(UpdateMutableBehaviour.class).onUpdate(thisBehaviour -> {
                Transform transform = thisBehaviour.get(Transform.class);
                transform.rotateY(radians(angle));
                // thisBehaviour.entity().destroy();
                // addOrRemoveRandomly(thisBehaviour.entity(), mesh);
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
                .range(LightRange.MEDIUM)); // 0.972, 0.796, 0.407
        light.add(Transform.class).position(0, 0, 0);
        light.add(MeshView.class).mesh(new Mesh(mesh.vertexData(),
                new PhongMaterial.Builder("LightMaterial").emissiveColor(Color.WHITE).build()));

        SceneManager.addScene(scene);
    }

    private ByteBuffer getCubeVertices() {

        float[] vertices = {
                // back face
                -1.0f, -1.0f, -1.0f,  0.0f,  0.0f, -1.0f, 0.0f, 0.0f, // bottom-left
                1.0f,  1.0f, -1.0f,  0.0f,  0.0f, -1.0f, 1.0f, 1.0f, // top-right
                1.0f, -1.0f, -1.0f,  0.0f,  0.0f, -1.0f, 1.0f, 0.0f, // bottom-right
                1.0f,  1.0f, -1.0f,  0.0f,  0.0f, -1.0f, 1.0f, 1.0f, // top-right
                -1.0f, -1.0f, -1.0f,  0.0f,  0.0f, -1.0f, 0.0f, 0.0f, // bottom-left
                -1.0f,  1.0f, -1.0f,  0.0f,  0.0f, -1.0f, 0.0f, 1.0f, // top-left
                // front face
                -1.0f, -1.0f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f, 0.0f, // bottom-left
                1.0f, -1.0f,  1.0f,  0.0f,  0.0f,  1.0f, 1.0f, 0.0f, // bottom-right
                1.0f,  1.0f,  1.0f,  0.0f,  0.0f,  1.0f, 1.0f, 1.0f, // top-right
                1.0f,  1.0f,  1.0f,  0.0f,  0.0f,  1.0f, 1.0f, 1.0f, // top-right
                -1.0f,  1.0f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f, 1.0f, // top-left
                -1.0f, -1.0f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f, 0.0f, // bottom-left
                // left face
                -1.0f,  1.0f,  1.0f, -1.0f,  0.0f,  0.0f, 1.0f, 0.0f, // top-right
                -1.0f,  1.0f, -1.0f, -1.0f,  0.0f,  0.0f, 1.0f, 1.0f, // top-left
                -1.0f, -1.0f, -1.0f, -1.0f,  0.0f,  0.0f, 0.0f, 1.0f, // bottom-left
                -1.0f, -1.0f, -1.0f, -1.0f,  0.0f,  0.0f, 0.0f, 1.0f, // bottom-left
                -1.0f, -1.0f,  1.0f, -1.0f,  0.0f,  0.0f, 0.0f, 0.0f, // bottom-right
                -1.0f,  1.0f,  1.0f, -1.0f,  0.0f,  0.0f, 1.0f, 0.0f, // top-right
                // right face
                1.0f,  1.0f,  1.0f,  1.0f,  0.0f,  0.0f, 1.0f, 0.0f, // top-left
                1.0f, -1.0f, -1.0f,  1.0f,  0.0f,  0.0f, 0.0f, 1.0f, // bottom-right
                1.0f,  1.0f, -1.0f,  1.0f,  0.0f,  0.0f, 1.0f, 1.0f, // top-right
                1.0f, -1.0f, -1.0f,  1.0f,  0.0f,  0.0f, 0.0f, 1.0f, // bottom-right
                1.0f,  1.0f,  1.0f,  1.0f,  0.0f,  0.0f, 1.0f, 0.0f, // top-left
                1.0f, -1.0f,  1.0f,  1.0f,  0.0f,  0.0f, 0.0f, 0.0f, // bottom-left
                // bottom face
                -1.0f, -1.0f, -1.0f,  0.0f, -1.0f,  0.0f, 0.0f, 1.0f, // top-right
                1.0f, -1.0f, -1.0f,  0.0f, -1.0f,  0.0f, 1.0f, 1.0f, // top-left
                1.0f, -1.0f,  1.0f,  0.0f, -1.0f,  0.0f, 1.0f, 0.0f, // bottom-left
                1.0f, -1.0f,  1.0f,  0.0f, -1.0f,  0.0f, 1.0f, 0.0f, // bottom-left
                -1.0f, -1.0f,  1.0f,  0.0f, -1.0f,  0.0f, 0.0f, 0.0f, // bottom-right
                -1.0f, -1.0f, -1.0f,  0.0f, -1.0f,  0.0f, 0.0f, 1.0f, // top-right
                // top face
                -1.0f,  1.0f, -1.0f,  0.0f,  1.0f,  0.0f, 0.0f, 1.0f, // top-left
                1.0f,  1.0f , 1.0f,  0.0f,  1.0f,  0.0f, 1.0f, 0.0f, // bottom-right
                1.0f,  1.0f, -1.0f,  0.0f,  1.0f,  0.0f, 1.0f, 1.0f, // top-right
                1.0f,  1.0f,  1.0f,  0.0f,  1.0f,  0.0f, 1.0f, 0.0f, // bottom-right
                -1.0f,  1.0f, -1.0f,  0.0f,  1.0f,  0.0f, 0.0f, 1.0f, // top-left
                -1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f // bottom-left
        };

        ByteBuffer vertexData = BufferUtils.createByteBuffer(vertices.length * FLOAT32.sizeof());

        for(float value : vertices) {
            vertexData.putFloat(value);
        }

        return vertexData.rewind();
    }

    @Override
    protected void onUpdate() {



    }

    private void addOrRemoveRandomly(Entity entity, Mesh mesh) {

        if(RAND.nextFloat() < 0.005f) {

            entity.destroy();

            final float angle = RAND.nextFloat();

            Entity model = entity.scene().newEntity();
            model.add(Transform.class).position(RAND.nextInt(200), -RAND.nextInt(200), -RAND.nextInt(200));
            model.add(MeshView.class).mesh(mesh);
            model.add(UpdateMutableBehaviour.class).onUpdate(thisBehaviour -> {
                Transform transform = thisBehaviour.get(Transform.class);
                transform.rotateY(radians(angle));
                addOrRemoveRandomly(thisBehaviour.entity(), mesh);
            });
        }
    }
}
