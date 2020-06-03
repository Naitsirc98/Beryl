package naitsirc98.beryl.examples.room;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.materials.PBRMetallicMaterial;
import naitsirc98.beryl.meshes.models.StaticModel;
import naitsirc98.beryl.meshes.models.StaticModelLoader;
import naitsirc98.beryl.meshes.views.StaticMeshView;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.StaticMeshInstance;
import org.joml.Vector3fc;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

import static naitsirc98.beryl.util.Maths.radians;

public class Room {

    private static final Path MODEL_ROOT_PATH = Paths.get("C:\\Users\\naits\\Downloads\\isometric-bedroom\\source\\Room");

    private static StaticModel roomModel;

    static {
        roomModel = new StaticModelLoader().load(MODEL_ROOT_PATH.resolve("Room.FBX"));
    }

    public static Entity create(Scene scene, Vector3fc position) {

        Entity room = scene.newEntity("Room");
        room.add(Transform.class).position(position).rotateX(radians(-90));
        room.add(StaticMeshInstance.class).meshViews(getMeshViews());

        return room;
    }

    private static Collection<StaticMeshView> getMeshViews() {
        return roomModel.meshes().stream()
                .map(m -> new StaticMeshView(m, getMaterialFor(m.name())))
                .collect(Collectors.toList());
    }

    private static Material getMaterialFor(String name) {

        switch(name) {
            case "Plane001":
                return getBaseRoomMaterial();
            case "Box010":
                return getFurnitureMaterial();
            case "Box012":
                return getObjectsMaterial();
            case "Plane007":
                return getPostersMaterial();
        }

        return PBRMetallicMaterial.getFactory().getDefault();
    }

    private static Material getPostersMaterial() {
        return PBRMetallicMaterial.getFactory().getMaterial("posters", material -> {
            material.albedoMap(loadTexture("mmm.jpg").setQuality(Texture.Quality.VERY_HIGH));
        });
    }

    private static Material getObjectsMaterial() {
        return PBRMetallicMaterial.getFactory().getMaterial("objects", material -> {
            material.albedoMap(loadTexture("Props_Base_Color.png"))
                    .normalMap(loadTexture("Props_Normal.png"))
                    .occlusionMap(loadTexture("Props_Mixed_AO.png"))
                    .metallicMap(loadTexture("Props_Metallic.png"))
                    .roughnessMap(loadTexture("Props_Roughness.png"));
        });
    }

    private static Material getFurnitureMaterial() {
        return PBRMetallicMaterial.getFactory().getMaterial("furniture", material -> {
            material.albedoMap(loadTexture("Muebles_Base_Color.png"))
                    .normalMap(loadTexture("Muebles_Normal.png"))
                    .occlusionMap(loadTexture("Muebles_Mixed_AO.png"))
                    .metallicMap(loadTexture("Muebles_Metallic.png"))
                    .roughnessMap(loadTexture("Muebles_Roughness.png"));
        });
    }

    private static Material getBaseRoomMaterial() {
        return PBRMetallicMaterial.getFactory().getMaterial("base room", material -> {
            material.albedoMap(loadTexture("Cuarto_Base_Color.png"))
                    .normalMap(loadTexture("Cuarto_Normal.png"))
                    .occlusionMap(loadTexture("Cuarto_Mixed_AO.png"))
                    .metallicMap(loadTexture("Cuarto_Metallic.png"))
                    .roughnessMap(loadTexture("Cuarto_Roughness.png"));
        });
    }

    private static Texture2D loadTexture(String name) {
        return GraphicsFactory.get().newTexture2D(MODEL_ROOT_PATH.resolve(name), PixelFormat.RGBA);
    }
}
