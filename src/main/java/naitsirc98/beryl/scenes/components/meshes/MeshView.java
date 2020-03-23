package naitsirc98.beryl.scenes.components.meshes;

import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.scenes.Component;
import naitsirc98.beryl.scenes.components.math.Transform;
import org.joml.Matrix4fc;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.Asserts.assertTrue;

public final class MeshView extends Component<MeshView> implements Iterable<Mesh> {

    private List<Mesh> meshes;
    private Map<Mesh, Material> materials;

    private MeshView() {

    }

    @Override
    protected void init() {
        super.init();
        meshes = new ArrayList<>(1);
        materials = new HashMap<>();
    }

    public Mesh mesh() {
        return mesh(0);
    }

    public Mesh mesh(int index) {
        return meshes.get(index);
    }

    public MeshView mesh(Mesh mesh) {
        return mesh(0, mesh);
    }

    public MeshView mesh(int index, Mesh mesh) {
        if(index == 0 && size() == 0) {
            addMesh(mesh);
        } else {
            meshes.set(index, requireNonNull(mesh));
        }
        return this;
    }

    public MeshView addMesh(Mesh mesh) {
        final int oldSize = size();
        meshes.add(requireNonNull(mesh));
        if(active() && oldSize == 0 && enabled()) {
            manager().enable(this);
        }
        return this;
    }

    public MeshView removeMesh(Mesh mesh) {
        return removeMesh(meshes.indexOf(mesh));
    }

    public MeshView removeMesh(int index) {
        meshes.remove(index);
        if(active() && size() == 0 && enabled()) {
            manager().disable(this);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Material> T material(Mesh mesh) {

        final Material material = materials.get(mesh);

        if(material == null) {
            return mesh.material();
        }

        return (T) material;
    }

    public <T extends Material> T material(int index) {
        return material(mesh(index));
    }

    public MeshView material(Material material) {
        return material(mesh(), material);
    }

    public MeshView material(Mesh mesh, Material material) {
        assertTrue(meshes.contains(mesh));
        materials.put(mesh, requireNonNull(material));
        if(active() && enabled()) {
            manager().addMaterial(material);
        }
        return this;
    }

    public MeshView material(int index, Material material) {
        return material(mesh(index), material);
    }

    public MeshView removeMaterial(Mesh mesh) {
        Material material = materials.remove(mesh);
        if(active() && enabled() && material != null) {
            manager().removeMaterial(material);
        }
        return this;
    }

    public MeshView removeMaterial(int index) {
        return removeMaterial(mesh(index));
    }

    public int size() {
        return meshes.size();
    }

    public MeshView clear() {
        meshes.clear();
        materials.clear();
        if(active() && enabled()) {
            manager().disable(this);
        }
        return this;
    }

    public final Matrix4fc modelMatrix() {
        return requires(Transform.class).modelMatrix();
    }

    public final Matrix4fc normalMatrix() {
        return requires(Transform.class).normalMatrix();
    }

    @Override
    public Iterator<Mesh> iterator() {
        return meshes.iterator();
    }

    @Override
    public Class<? extends Component> type() {
        return MeshView.class;
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    protected MeshView self() {
        return this;
    }

    @Override
    protected void onDestroy() {

        meshes.clear();
        materials.clear();

        meshes = null;
        materials = null;
    }

    @Override
    protected MeshViewManager manager() {
        return (MeshViewManager) super.manager();
    }

    List<Mesh> meshes() {
        return meshes;
    }

    Collection<Material> materials() {
        return materials.values();
    }
}
