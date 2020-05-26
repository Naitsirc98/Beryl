package naitsirc98.beryl.examples.pbr;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.meshes.models.StaticModel;
import naitsirc98.beryl.meshes.models.StaticModelLoader;
import naitsirc98.beryl.meshes.views.StaticMeshView;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.StaticMeshInstance;
import org.joml.Vector3fc;

import java.nio.file.Path;

import static naitsirc98.beryl.util.Maths.radians;

public class CerberusRevolver {

    private static StaticModel cerberusModel;

    static {

        Path modelPath = BerylFiles.getPath("modelse/Cerberus_by_Andrew_Maximov/Cerberus_LP.FBX");

        if(modelPath != null) {
            cerberusModel = new StaticModelLoader().load(modelPath);
        }
    }

    public static Entity create(Scene scene, Vector3fc position, float scale) {

        Entity entity = scene.newEntity();
        entity.add(Transform.class).position(position).scale(scale).rotateX(radians(-90));

        if(cerberusModel != null) {
            entity.add(StaticMeshInstance.class).meshView(getMeshView());
        }

        return entity;
    }

    private static StaticMeshView getMeshView() {
        return new StaticMeshView(cerberusModel.mesh(0),
                PBRDemoUtils.getPBRMetallicMaterialFromFolder(
                        BerylFiles.getPath("textures/Cerberus_by_Andrew_Maximov"), "tga"));
    }

}
