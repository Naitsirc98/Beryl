package naitsirc98.beryl.examples.app1;

import naitsirc98.beryl.meshes.models.StaticModel;
import naitsirc98.beryl.meshes.models.StaticModelLoader;
import naitsirc98.beryl.meshes.views.StaticMeshView;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.math.Transform;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class HelicopterFactory {

    public static Entity createUH64(Scene scene) {

        Entity helicopter = scene.newEntity();

        helicopter.add(Transform.class);


        return helicopter;
    }

    private static StaticModel getUH64Model() {
        return StaticModelLoader.get().load(Paths.get("C:\\Users\\naits\\Downloads\\5xulumjp2ohs-SeaHawk\\Seahawk.obj"));
    }
}
