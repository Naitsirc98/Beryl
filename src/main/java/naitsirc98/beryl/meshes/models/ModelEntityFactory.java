package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.graphics.rendering.PrimitiveTopology;
import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.meshes.MeshFactory;
import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.MeshView;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.graphics.rendering.PrimitiveTopology.TRIANGLES;
import static naitsirc98.beryl.util.types.DataType.INT32;

public class ModelEntityFactory {

    private final Model model;
    private final Map<String, Mesh> meshes;
    private Function<String, Material> materialsFunction;
    private MeshFactory<Model.Mesh> meshMeshFactory;

    public ModelEntityFactory(Model model) {
        this.model = requireNonNull(model);
        meshes = new HashMap<>();
        materialsFunction = meshName -> PhongMaterial.getDefault();
        meshMeshFactory = this::createMeshFromModelMesh;
    }

    public Model model() {
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

        MeshView meshView = entity.add(MeshView.class);

        for(int i = 0;i < node.numMeshes();i++) {
            meshView.addMesh(getMesh(node.mesh(i)));
        }
    }

    private Mesh getMesh(Model.Mesh mesh) {
        return meshes.computeIfAbsent(mesh.name(), name -> meshMeshFactory.create(mesh, materialsFunction.apply(name)));
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

    public ModelEntityFactory materialsFunction(Function<String, Material> materialsFunction) {
        this.materialsFunction = requireNonNull(materialsFunction);
        return this;
    }

    public ModelEntityFactory meshFactory(MeshFactory<Model.Mesh> meshMeshFactory) {
        this.meshMeshFactory = requireNonNull(meshMeshFactory);
        return this;
    }

    private Mesh createMeshFromModelMesh(Model.Mesh modelMesh, Material material) {

        VertexData.Builder builder = VertexData.builder(modelMesh.vertexLayout(), TRIANGLES);

        for(int i = 0;i < modelMesh.vertices().length;i++) {
            builder.vertices(i, modelMesh.vertices()[i]);
        }

        if(modelMesh.indices() != null) {
            builder.indices(modelMesh.indices(), INT32);
        }

        return new Mesh(builder.build(), material);
    }

}
