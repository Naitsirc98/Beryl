package naitsirc98.beryl.scenes.components.meshes;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.meshes.v2.MeshView;
import naitsirc98.beryl.scenes.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static naitsirc98.beryl.util.Asserts.assertTrue;

public class MeshInstance extends Component<MeshInstance> implements Iterable<MeshView> {

    private static final List<MeshView> EMPTY = Collections.emptyList();

    private List<MeshView> meshViews;

    private MeshInstance() {

    }

    @Override
    protected void init() {
        super.init();
        meshViews = EMPTY;
    }

    public MeshView meshView(int index) {
        return meshViews.get(index);
    }

    public int numMeshViews() {
        return meshViews.size();
    }

    public MeshInstance meshViews(MeshView meshView) {
        if(meshViews != EMPTY) {
            Log.error("Cannot modify Mesh Instance component once you have set its Mesh Views");
        } else {
            assertNonNull(meshView);
            meshViews = Collections.singletonList(meshView);
            onEnable();
        }
        return this;
    }

    public MeshInstance meshViews(MeshView... meshViews) {
        if(this.meshViews != EMPTY) {
            Log.error("Cannot modify Mesh Instance component once you have set its Mesh Views");
        } else {
            assertNonNull(meshViews);
            assertTrue(meshViews.length > 0);
            this.meshViews = new ArrayList<>(meshViews.length);
            for(MeshView meshView : meshViews) {
                assertNonNull(meshView);
                this.meshViews.add(meshView);
            }
            onEnable();
        }
        return this;
    }

    @Override
    public Class<? extends Component> type() {
        return MeshInstance.class;
    }

    @Override
    public boolean enabled() {
        return super.enabled() && meshViews != EMPTY;
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
}
