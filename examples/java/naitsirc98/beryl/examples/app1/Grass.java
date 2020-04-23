package naitsirc98.beryl.examples.app1;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.meshes.TerrainMesh;
import naitsirc98.beryl.meshes.models.StaticModel;
import naitsirc98.beryl.meshes.models.StaticModelLoader;
import naitsirc98.beryl.meshes.models.StaticVertexHandler;
import naitsirc98.beryl.meshes.views.StaticMeshView;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.StaticMeshInstance;

import java.util.Random;

public class Grass {

    private static StaticModel grassModel;

    public static StaticModel getGrassModel() {

        if(grassModel == null) {

            grassModel = StaticModelLoader.get().load(BerylFiles.getPath("models/grass.obj"), false,
                    new StaticVertexHandler.Builder().normalFunction(n -> n.set(0, 1, 0)).build());

            setGrassMaterial(grassModel.meshView(0));
        }

        return grassModel;
    }

    public static Entity create(Scene scene, float scale) {

        Entity grass = scene.newEntity();

        grass.add(Transform.class).scale(scale);

        grass.add(StaticMeshInstance.class).meshView(getGrassModel().meshView(0));

        return grass;
    }

    public static void placeGrassAtRandomPositions(Scene scene, TerrainMesh terrain, int terrainSize, float scale, int grassCount) {

        Random random = new Random();

        for(int i = 0;i < grassCount;i++) {

            Entity grass = create(scene, scale);

            final float x = random.nextInt(terrainSize);
            final float z = random.nextInt(terrainSize);
            final float y = terrain.heightAt(0, 0, x, z);

            grass.get(Transform.class).position(x, y, z);
        }

    }

    public static void setGrassMaterial(StaticMeshView meshView) {
        PhongMaterial material = (PhongMaterial) meshView.material();
        Texture2D colorTexture = GraphicsFactory.get().newTexture2D(BerylFiles.getString("textures/grass.png"), PixelFormat.SRGBA);
        colorTexture.setQuality(Texture.Quality.MEDIUM);
        material.colorMap(colorTexture);
    }

}
