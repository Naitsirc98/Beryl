package naitsirc98.beryl.examples.forest;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.meshes.models.StaticModel;
import naitsirc98.beryl.meshes.models.StaticModelLoader;
import naitsirc98.beryl.meshes.models.StaticVertexHandler;
import naitsirc98.beryl.meshes.views.StaticMeshView;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.StaticMeshInstance;
import naitsirc98.beryl.util.Color;

import java.nio.file.Paths;

public class Lamp {

    public static final String LAMP_NAME = "Lamp";

    private static StaticModel lampModel;

    public static StaticModel getLampModel() {

        if(lampModel == null) {

            StaticModelLoader loader = new StaticModelLoader();

            lampModel = loader.load(BerylFiles.getPath("models/lamp.fbx"),
                            false, new StaticVertexHandler.Builder().positionFunction(p -> p.mul(0.01f)).build());

            setLampMaterial(lampModel.meshView(0));
        }

        return lampModel;
    }


    public static Entity create(Scene scene, float x, float y, float z, float scale) {

        Entity lamp = scene.newEntity(LAMP_NAME);
        lamp.get(Transform.class).position(x, y, z).scale(scale);
        lamp.add(StaticMeshInstance.class).meshView(getLampModel().meshView(0));

        return lamp;
    }

    private static void setLampMaterial(StaticMeshView meshView) {

        PhongMaterial material = (PhongMaterial) meshView.material();

        Texture2D colorTexture = GraphicsFactory.get().newTexture2D(getTexturePath("lightning1_lightoff_BaseColor.tga"), PixelFormat.SRGBA);
        colorTexture.setQuality(Texture.Quality.HIGH);

        Texture2D normalMap = GraphicsFactory.get().newTexture2D(getTexturePath("lightning1_Normal.tga"), PixelFormat.RGBA);
        normalMap.setQuality(Texture.Quality.HIGH);

        Texture2D emissiveMap = GraphicsFactory.get().newTexture2D(getTexturePath("lightning1_Emissive.tga"), PixelFormat.RGBA);
        emissiveMap.setQuality(Texture.Quality.HIGH);

        material.colorMap(colorTexture)
                .normalMap(normalMap)
                .emissiveMap(emissiveMap)
                .emissiveColor(Color.colorWhite());
    }

    private static String getTexturePath(String name) {
        return BerylFiles.getString("textures/lamp/"+name);
    }

}
