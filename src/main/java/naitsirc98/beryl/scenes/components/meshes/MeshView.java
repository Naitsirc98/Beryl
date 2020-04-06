package naitsirc98.beryl.scenes.components.meshes;

import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.scenes.Component;
import naitsirc98.beryl.scenes.components.math.Transform;
import org.joml.Matrix4fc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public final class MeshView extends Component<MeshView> implements Iterable<Mesh> {

    private List<Mesh> meshes;
    private boolean castShadows;

    private MeshView() {

    }

    @Override
    protected void init() {
        super.init();
        meshes = new ArrayList<>(1);
        castShadows = true;
    }

    public boolean castShadows() {
        return castShadows;
    }

    public MeshView castShadows(boolean castShadows) {
        this.castShadows = castShadows;
        return this;
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
        } else if(!meshes.get(index).equals(mesh)){
            final Mesh old = meshes.get(index);
            meshes.set(index, requireNonNull(mesh));
            if(active()) {
                manager().removeMeshInstance(old, this);
                manager().addMeshInstance(mesh, this);
            }
            markModified();
        }
        return this;
    }

    public MeshView addMesh(Mesh mesh) {
        final int oldSize = size();
        meshes.add(requireNonNull(mesh));
        if(active() && oldSize == 0 && enabled()) {
            manager().enable(this);
        }
        if(active()) {
            manager().addMeshInstance(mesh, this);
        }
        markModified();
        return this;
    }

    public MeshView removeMesh(Mesh mesh) {
        return removeMesh(meshes.indexOf(mesh));
    }

    public MeshView removeMesh(int index) {
        final Mesh old = meshes.remove(index);
        if(active() && size() == 0 && enabled()) {
            manager().disable(this);
        }
        if(active()) {
            manager().removeMeshInstance(old, this);
        }
        markModified();
        return this;
    }

    public int size() {
        return meshes.size();
    }

    public MeshView clear() {
        meshes.clear();
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
        meshes = null;
    }

    @Override
    protected MeshViewManager manager() {
        return (MeshViewManager) super.manager();
    }

    List<Mesh> meshes() {
        return meshes;
    }

    Stream<Material> materials() {
        return meshes.stream().map(Mesh::material);
    }

    private void markModified() {
        doLater(() -> manager().markModified());
    }
}
