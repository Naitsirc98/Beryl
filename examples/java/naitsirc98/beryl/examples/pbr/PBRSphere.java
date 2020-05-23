package naitsirc98.beryl.examples.pbr;

import naitsirc98.beryl.core.Time;
import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Sampler;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.materials.PBRMetallicMaterial;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.meshes.views.StaticMeshView;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.behaviours.UpdateMutableBehaviour;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.StaticMeshInstance;

import java.nio.file.Path;

import static naitsirc98.beryl.util.Maths.radians;

public class PBRSphere {

    public static Entity create(Scene scene, float x, float y, float z, Path texturesPath) {

        StaticMesh sphere = StaticMesh.sphere();//SphereMesh.create("SphereMeshPBR", 64, 64);

        Entity entity = scene.newEntity();

        StaticMeshView view = new StaticMeshView(sphere, getPBRMetallicMaterialFromFolder(texturesPath));

        entity.add(Transform.class).position(x, y, z).scale(1);
        entity.add(StaticMeshInstance.class).meshView(view);
        entity.add(UpdateMutableBehaviour.class).onUpdate(self -> {
           self.get(Transform.class).rotateY(radians(Time.time()));
        });

        return entity;
    }

    private static PBRMetallicMaterial getPBRMetallicMaterialFromFolder(Path folder) {

        return PBRMetallicMaterial.getFactory().getMaterial(folder.toString(), material -> {

            material.setAlbedoMap(loadTexture(folder.resolve("albedo.png")));
            material.setNormalMap(loadTexture(folder.resolve("normal.png")));
            material.setMetallicMap(loadTexture(folder.resolve("metallic.png")));
            material.setRoughnessMap(loadTexture(folder.resolve("roughness.png")));
            material.setOcclusionMap(loadTexture(folder.resolve("ao.png")));
        });
    }

    private static Texture2D loadTexture(Path path) {

        Texture2D texture = GraphicsFactory.get().newTexture2D(path.toString(), PixelFormat.RGBA);

        texture.sampler()
                .wrapMode(Sampler.WrapMode.REPEAT)
                .minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR)
                .magFilter(Sampler.MagFilter.LINEAR)
                .lodBias(-1);

        texture.generateMipmaps();

        return texture;
    }


}
