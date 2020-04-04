package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.MeshView;

import java.util.function.Function;

public final class ModelUtils {

    public static Entity modelToEntity(Model model, Scene scene, Function<String, Material> materialFunction) {

        Model.Node node = model.root();

        Entity entity = newEntityFromNode(node, scene);

        processNode(node, entity, materialFunction);

        return entity;
    }

    private static void processNode(Model.Node node, Entity entity, Function<String, Material> materialFunction) {

        processNodeMeshes(node, entity, materialFunction);

        processNodeChildren(node, entity, materialFunction);
    }

    private static void processNodeMeshes(Model.Node node, Entity entity, Function<String, Material> materialFunction) {

        if(node.numMeshes() == 0) {
            return;
        }

        MeshView meshView = entity.add(MeshView.class);

        for(int i = 0;i < node.numMeshes();i++) {
            Model.Mesh mesh = node.mesh(i);
            meshView.mesh(i, new Mesh(mesh.createVertexData(), materialFunction.apply(mesh.name())));
        }
    }

    private static void processNodeChildren(Model.Node node, Entity entity, Function<String, Material> materialFunction) {

        if(node.numChildren() == 0) {
            return;
        }

        Transform transform = entity.get(Transform.class);

        for(int i = 0;i < node.numChildren();i++) {
            Model.Node childNode = node.child(i);
            Entity childEntity = newEntityFromNode(childNode, entity.scene());
            transform.addChild(childEntity.get(Transform.class));
            processNode(childNode, childEntity, materialFunction);
        }

        transform.transformation(node.transformation());
    }

    private static Entity newEntityFromNode(Model.Node node, Scene scene) {

        Entity entity = scene.newEntity(node.name());

        entity.add(Transform.class);

        return entity;
    }

    private ModelUtils() {}
}
