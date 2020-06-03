package naitsirc98.beryl.examples.forest;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.materials.Material;
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
    private static StaticMeshView grassMeshView;

    static {

        grassModel = new StaticModelLoader().load(BerylFiles.getPath("models/grass.obj"),
                new StaticVertexHandler.Builder().normalFunction(n -> n.set(0, 1, 0)).build());

        grassMeshView = new StaticMeshView(grassModel.mesh(0), getGrassMaterial());
    }

    public static StaticModel getGrassModel() {
        return grassModel;
    }


    public static Entity create(Scene scene, float scale) {

        Entity grass = scene.newEntity();

        grass.add(Transform.class).scale(scale);

        grass.add(StaticMeshInstance.class).meshView(grassMeshView);

        return grass;
    }

    public static void placeGrassUnderWater(Scene scene, TerrainMesh terrain, int terrainSize, float scale, float waterSurfaceY, int grassCount) {

        Random random = new Random();

        for(int i = 0;i < grassCount;i++) {

            Entity grass = create(scene, scale);

            float x;
            float z;
            float y;

            do {

                x = random.nextInt(terrainSize);
                z = random.nextInt(terrainSize);
                y = terrain.heightMap().heightAt(0, 0, x, z);

            } while(y >= waterSurfaceY);

            grass.get(Transform.class).position(x, y, z);
        }

    }

    public static Material getGrassMaterial() {
        PhongMaterial material = PhongMaterial.getFactory().getMaterial("GrassMaterial");
        Texture2D colorTexture = GraphicsFactory.get().newTexture2D(BerylFiles.getPath("textures/grass.png"), PixelFormat.SRGBA);
        colorTexture.setQuality(Texture.Quality.MEDIUM);
        material.colorMap(colorTexture);
        return material;
    }

}
