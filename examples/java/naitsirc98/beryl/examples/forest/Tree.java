package naitsirc98.beryl.examples.forest;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.core.Time;
import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.meshes.TerrainMesh;
import naitsirc98.beryl.meshes.models.StaticModel;
import naitsirc98.beryl.meshes.models.StaticModelLoader;
import naitsirc98.beryl.meshes.models.StaticVertexHandler;
import naitsirc98.beryl.meshes.views.StaticMeshView;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.behaviours.UpdateBehaviour;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.StaticMeshInstance;

import java.nio.file.Path;
import java.util.Random;

import static naitsirc98.beryl.util.Maths.clamp;
import static naitsirc98.beryl.util.Maths.sin;

public class Tree {

    private static StaticModel treeModel;
    private static StaticMeshView trunkMeshView;
    private static StaticMeshView capMeshView;
    private static StaticMeshView leafMeshView;

    static {

        Path modelPath = BerylFiles.getPath("models/tree.obj");

        StaticVertexHandler handlerToMakeModelSmaller = new StaticVertexHandler.Builder()
                .positionFunction(p -> p.mul(0.012f))
                .build();

        treeModel = new StaticModelLoader().load(modelPath, handlerToMakeModelSmaller);

        Log.warning(treeModel);

        trunkMeshView = getTrunkMeshView();
        capMeshView = getCapMeshView();
        leafMeshView = getLeafMeshView();
    }

    public static StaticModel getTreeModel() {
        return treeModel;
    }

    public static Entity create(Scene scene, String name) {

        Entity tree = scene.newEntity(name);
        tree.add(Transform.class);
        tree.add(StaticMeshInstance.class).meshViews(trunkMeshView, capMeshView, leafMeshView);
        tree.add(TreeRandomBouncing.class);

        return tree;
    }

    public static void createRandomForest(Scene scene, TerrainMesh terrainMesh, int terrainSize, float minY, int treeCount) {

        Random random = new Random();

        for(int i = 0;i < treeCount;i++) {

            Entity tree = create(scene, "Tree" + i);

            float x;
            float z;
            float y;

            do {

                x = random.nextInt(terrainSize);
                z = random.nextInt(terrainSize);
                y = terrainMesh.heightMap().heightAt(0, 0, x, z);

            } while(y <= minY);

            tree.get(Transform.class).position(x, y - 1, z);
        }
    }

    private static StaticMeshView getTrunkMeshView() {
        String name = "conifer_macedonian_pine_5";
        return new StaticMeshView(treeModel.mesh(name), PhongMaterial.getFactory().getMaterial(name, material -> {
            GraphicsFactory g = GraphicsFactory.get();
            material.setColorMap(g.newTexture2D(getTexturePath("Bark_Color.png"), PixelFormat.SRGBA).setQuality(Texture.Quality.MEDIUM));
        }));
    }

    private static StaticMeshView getCapMeshView() {
        String name = "/Game/Cap_Branch_Mat_Cap_Branch_Mat";
        return new StaticMeshView(treeModel.mesh(name), PhongMaterial.getFactory().getMaterial(name, material -> {
            GraphicsFactory g = GraphicsFactory.get();
            material.setColorMap(g.newTexture2D(getTexturePath("Cap_Color.png"), PixelFormat.SRGBA).setQuality(Texture.Quality.LOW));
        }));
    }

    private static StaticMeshView getLeafMeshView() {
        String name = "/Game/conifer_macedonian_pine_Leaf_Mat_conifer_macedonian_pine_Leaf_Mat";
        return new StaticMeshView(treeModel.mesh(name), PhongMaterial.getFactory().getMaterial(name, material -> {
            GraphicsFactory g = GraphicsFactory.get();
            material.setColorMap(g.newTexture2D(getTexturePath("Leaf_Color.png"), PixelFormat.SRGBA).setQuality(Texture.Quality.HIGH));
            material.getDiffuseMap().sampler().lodBias(-1.5f);
        }));
    }

    private static String getTexturePath(String textureName) {
        return BerylFiles.getString("textures/tree/"+textureName);
    }

    public static class TreeRandomBouncing extends UpdateBehaviour {

        private static final float BOUNCING_LIMIT = 95.0f;

        private float bouncingLimit;
        private float normalBouncingLimit;
        private float normalBouncingSpeed;
        private float bouncingSpeed;

        @Override
        protected void onInit() {
            normalBouncingSpeed = bouncingSpeed = clamp(0.35f, 1.2f, (float) Math.random());
            normalBouncingLimit = bouncingLimit = 89 + new Random().nextInt(11);
        }

        public void setBouncingSpeed(float bouncingSpeed) {
            this.bouncingSpeed = clamp(normalBouncingSpeed, 10.0f, bouncingSpeed);
        }

        public void setBouncingLimit(float bouncingLimit) {
            this.bouncingLimit = clamp(89, normalBouncingLimit, bouncingLimit);
        }

        @Override
        public void onUpdate() {
            get(Transform.class).rotateZ(sin(Time.time() * bouncingSpeed) / bouncingLimit);
        }
    }
}
