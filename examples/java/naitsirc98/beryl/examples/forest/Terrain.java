package naitsirc98.beryl.examples.forest;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.meshes.TerrainMesh;
import naitsirc98.beryl.meshes.TerrainMeshLoader;
import naitsirc98.beryl.meshes.views.StaticMeshView;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.StaticMeshInstance;

public class Terrain {

    public static final float TERRAIN_SIZE = 800;

    public static final String TERRAIN_HEIGHTMAP = BerylFiles.getString("textures/terrain_heightmap.png");

    public static TerrainMesh getTerrainMesh() {
        return TerrainMeshLoader.get().load("Terrain", TERRAIN_HEIGHTMAP, TERRAIN_SIZE);
    }

    public static Entity create(Scene scene) {

        TerrainMesh terrainMesh = getTerrainMesh();

        Entity terrain = scene.newEntity();
        terrain.add(Transform.class).position(0, 0, 0).scale(1);
        terrain.add(StaticMeshInstance.class).meshView(new StaticMeshView(terrainMesh, getTerrainMaterial()));

        return terrain;
    }

    private static PhongMaterial getTerrainMaterial() {

        return PhongMaterial.getFactory().getMaterial("Terrain", material -> {

            Texture2D colorMap = GraphicsFactory.get()
                    .newTexture2D(BerylFiles.getString("textures/terrain_grass.jpg"), PixelFormat.SRGBA);

            colorMap.setQuality(Texture.Quality.HIGH);

            material.colorMap(colorMap)
                    // .setShininess(1)
                    .tiling(48, 48);
        });
    }

}
