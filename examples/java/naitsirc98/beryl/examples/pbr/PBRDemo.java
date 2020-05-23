package naitsirc98.beryl.examples.pbr;

import naitsirc98.beryl.core.*;
import naitsirc98.beryl.examples.common.CameraController;
import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.rendering.ShadingModel;
import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.graphics.textures.Texture.Quality;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.lights.LightRange;
import naitsirc98.beryl.lights.PointLight;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.materials.PBRMetallicMaterial;
import naitsirc98.beryl.meshes.SphereMesh;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.meshes.views.StaticMeshView;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.SceneManager;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.StaticMeshInstance;
import naitsirc98.beryl.scenes.environment.SceneEnvironment;
import naitsirc98.beryl.scenes.environment.skybox.Skybox;
import naitsirc98.beryl.scenes.environment.skybox.SkyboxFactory;
import naitsirc98.beryl.util.Color;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

import static naitsirc98.beryl.scenes.Fog.DEFAULT_FOG_DENSITY;
import static org.lwjgl.opengl.GL11.GL_RED;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.GL_RGB;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.GL_R8;
import static org.lwjgl.opengl.GL45C.*;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.system.MemoryStack.stackPush;

public class PBRDemo extends BerylApplication {

    public PBRDemo() {
        BerylConfiguration.SHADOWS_ENABLED_ON_START.set(false);
        BerylConfiguration.DEFAULT_SHADING_MODEL.set(ShadingModel.PBR_METALLIC);
        BerylConfiguration.SET_CONFIGURATION_METHOD.set(DefaultConfigurations.debugConfiguration());
        BerylConfiguration.PRINT_SHADERS_SOURCE.set(false);
        BerylConfiguration.OPENGL_ENABLE_WARNINGS_UNIFORMS.set(false);
    }

    @Override
    protected void onStart() {

        Scene scene = SceneManager.newScene("PBR Demo");

        setupCamera(scene);

        setSceneEnvironment(scene);
        
        setSceneObjects(scene);

        SceneManager.setScene(scene);
    }

    private void setSceneObjects(Scene scene) {

        StaticMesh sphere = SphereMesh.create("SphereMeshPBR", 64, 64);

        Entity entity = scene.newEntity();

        StaticMeshView view = new StaticMeshView(sphere, getPBRMetallicMaterialFromFolder(BerylFiles.getPath("textures/gold")));

        entity.add(Transform.class).position(0, 0, -40).scale(1);
        entity.add(StaticMeshInstance.class).meshView(view);

    }

    private PBRMetallicMaterial getPBRMetallicMaterialFromFolder(Path folder) {

        return PBRMetallicMaterial.getFactory().getMaterial(folder.toString(), material -> {

            GraphicsFactory g = GraphicsFactory.get();

            material.setAlbedoMap(g.newTexture2D(folder.resolve("albedo.png").toString(), PixelFormat.RGBA).setQuality(Quality.MEDIUM));
            material.setNormalMap(g.newTexture2D(folder.resolve("normal.png").toString(), PixelFormat.RGBA).setQuality(Quality.MEDIUM));
            material.setMetallicMap(g.newTexture2D(folder.resolve("metallic.png").toString(), PixelFormat.RGB).setQuality(Quality.MEDIUM));
            material.setRoughnessMap(g.newTexture2D(folder.resolve("roughness.png").toString(), PixelFormat.RGB).setQuality(Quality.MEDIUM));
            material.setOcclusionMap(g.newTexture2D(folder.resolve("ao.png").toString(), PixelFormat.RGB).setQuality(Quality.MEDIUM));
        });
    }

    private void setupCamera(Scene scene) {

        Entity cameraController = scene.newEntity();

        cameraController.add(CameraController.class);
    }

    private void setSceneEnvironment(Scene scene) {

        SceneEnvironment environment = scene.environment();

        Skybox skybox = SkyboxFactory.newSkyboxHDR("G:\\JavaDevelopment\\Quasar\\src\\main\\resources\\resources\\textures\\hdr\\newport_loft.hdr");

        PointLight light = new PointLight().position(0, 0, 10).color(Color.colorWhite().intensify(10));

        environment.lighting().pointLights().add(light);
        environment.skybox(skybox);
        environment.ambientColor(new Color(0.5f));
        environment.fog().density(DEFAULT_FOG_DENSITY);
    }
}
