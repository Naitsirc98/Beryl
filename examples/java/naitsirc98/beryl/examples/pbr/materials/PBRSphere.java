package naitsirc98.beryl.examples.pbr.materials;

import naitsirc98.beryl.core.Time;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.meshes.views.StaticMeshView;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.behaviours.UpdateMutableBehaviour;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.StaticMeshInstance;

import java.nio.file.Path;

import static naitsirc98.beryl.examples.pbr.PBRDemoUtils.getPBRMetallicMaterialFromFolder;
import static naitsirc98.beryl.util.Maths.radians;

public class PBRSphere {

    public static Entity create(Scene scene, float x, float y, float z, Path texturesPath) {

        StaticMesh sphere = StaticMesh.sphere();//SphereMesh.create("SphereMeshPBR", 64, 64);

        Entity entity = scene.newEntity();

        StaticMeshView view = new StaticMeshView(sphere, getPBRMetallicMaterialFromFolder(texturesPath, "png"));

        entity.add(Transform.class).position(x, y, z).scale(1);
        entity.add(StaticMeshInstance.class).meshView(view);
        entity.add(UpdateMutableBehaviour.class).onUpdate(self -> {
           self.get(Transform.class).rotateY(radians(Time.time() * 1.5f));
        });

        return entity;
    }

}
