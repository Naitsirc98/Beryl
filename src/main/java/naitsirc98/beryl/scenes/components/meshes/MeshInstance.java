package naitsirc98.beryl.scenes.components.meshes;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.meshes.MeshView;
import naitsirc98.beryl.scenes.Component;
import naitsirc98.beryl.scenes.components.math.Transform;
import org.joml.Matrix4fc;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static naitsirc98.beryl.util.Asserts.assertTrue;

public class MeshInstance extends Component<MeshInstance> implements Iterable<MeshView> {

    private List<MeshView> meshViews;

    private MeshInstance() {

    }

    @Override
    protected void init() {
        super.init();
        meshViews = null;
    }

    public MeshView meshView(int index) {
        return meshViews.get(index);
    }

    public int numMeshViews() {
        return meshViews.size();
    }

    public MeshInstance meshView(MeshView meshView) {
        return meshViews(meshView);
    }

    public MeshInstance meshViews(MeshView meshView) {
        if(meshViews != null) {
            Log.error("Cannot modify Mesh Instance component once you have set its Mesh Views");
        } else {
            assertNonNull(meshView);
            this.meshViews = Collections.singletonList(meshView);
            doLater(() -> manager().enable(this));
        }
        return this;
    }

    public MeshInstance meshViews(MeshView... meshViews) {
        if(this.meshViews != null) {
            Log.error("Cannot modify Mesh Instance component once you have set its Mesh Views");
        } else {
            assertNonNull(meshViews);
            assertTrue(meshViews.length > 0);
            this.meshViews = Arrays.stream(meshViews).filter(Objects::nonNull).distinct().collect(Collectors.toUnmodifiableList());
            doLater(() -> manager().enable(this));
        }
        return this;
    }

    public Matrix4fc modelMatrix() {
        return requires(Transform.class).modelMatrix();
    }

    public Matrix4fc normalMatrix() {
        return requires(Transform.class).normalMatrix();
    }

    @Override
    public Class<? extends Component> type() {
        return MeshInstance.class;
    }

    @Override
    public boolean enabled() {
        return super.enabled() && meshViews != null;
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    protected MeshInstance self() {
        return this;
    }

    @Override
    protected void onDestroy() {

    }

    public Stream<MeshView> meshViews() {
        return meshViews.stream();
    }

    @Override
    public Iterator<MeshView> iterator() {
        return meshViews.iterator();
    }

    @Override
    protected MeshInstanceManager manager() {
        return (MeshInstanceManager) super.manager();
    }
}
