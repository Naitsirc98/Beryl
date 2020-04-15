package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.materials.IMaterial;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.meshes.views.StaticMeshView;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.StaticMeshInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class StaticModelEntityFactory {

    private final Model<StaticMesh> model;
    private final Map<String, StaticMeshView> meshViews;
    private Function<String, IMaterial> materialsFunction;
    private MeshViewFactory<ModelMesh<StaticMesh>, StaticMeshView> meshMeshFactory;

    public StaticModelEntityFactory(Model<StaticMesh> model) {
        this.model = requireNonNull(model);
        meshViews = new HashMap<>();
        materialsFunction = meshName -> PhongMaterial.getDefault();
        meshMeshFactory = this::createMeshViewFromModelMesh;
    }

    public Model<StaticMesh> model() {
        return model;
    }

    public Entity newEntity(Scene scene) {
        return newEntity(null, scene);
    }

    public Entity newEntity(String prefix, Scene scene) {

        Model.Node node = model.root();

        Entity entity = newEntityFromNode(prefix, node, scene);

        processNode(prefix, node, entity);

        return entity;
    }

    private void processNode(String prefix, Model.Node node, Entity entity) {

        processNodeMeshes(node, entity);

        processNodeChildren(prefix, node, entity);
    }

    private void processNodeMeshes(Model.Node node, Entity entity) {

        if(node.numMeshes() == 0) {
            return;
        }

        StaticMeshInstance meshInstance = entity.add(StaticMeshInstance.class);

        StaticMeshView[] meshViews = new StaticMeshView[node.numMeshes()];

        for(int i = 0;i < node.numMeshes();i++) {
            meshViews[i] = getMeshView(node.mesh(i));
        }

        meshInstance.meshViews(meshViews);
    }

    private StaticMeshView getMeshView(ModelMesh<StaticMesh> loadedMesh) {
        return meshViews.computeIfAbsent(loadedMesh.name(), name -> meshMeshFactory.create(loadedMesh, materialsFunction.apply(name)));
    }

    private void processNodeChildren(String prefix, Model.Node node, Entity entity) {

        if(node.numChildren() == 0) {
            return;
        }

        Transform transform = entity.get(Transform.class);

        for(int i = 0;i < node.numChildren();i++) {
            Model.Node childNode = node.child(i);
            Entity childEntity = newEntityFromNode(prefix, childNode, entity.scene());
            transform.addChild(childEntity.get(Transform.class));
            processNode(prefix, childNode, childEntity);
        }

        transform.transformation(node.transformation());
    }

    private Entity newEntityFromNode(String prefix, Model.Node node, Scene scene) {

        Entity entity = prefix == null ? scene.newEntity() : scene.newEntity(prefix + node.name());

        entity.add(Transform.class);

        return entity;
    }

    public StaticModelEntityFactory materialsFunction(Function<String, IMaterial> materialsFunction) {
        this.materialsFunction = requireNonNull(materialsFunction);
        return this;
    }

    public StaticModelEntityFactory meshFactory(MeshViewFactory<ModelMesh<StaticMesh>, StaticMeshView> meshMeshFactory) {
        this.meshMeshFactory = requireNonNull(meshMeshFactory);
        return this;
    }

    private StaticMeshView createMeshViewFromModelMesh(ModelMesh<StaticMesh> modelLoadedMesh, IMaterial material) {
        return new StaticMeshView(modelLoadedMesh.mesh(), material);
    }

}
