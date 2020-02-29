package naitsirc98.beryl.scenes.components.meshes;

import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.scenes.Component;
import naitsirc98.beryl.scenes.components.math.Transform;

public final class MeshView extends Component<MeshView> {

    private Mesh mesh;

    private MeshView() {

    }

    public Mesh mesh() {
        return mesh;
    }

    public MeshView mesh(Mesh mesh) {
        if(this.mesh != null && mesh == null) {
            disable();
        } else if(this.mesh == null && mesh != null) {
            enabled();
        }
        this.mesh = mesh;
        return this;
    }

    public final Transform transform() {
        return requires(Transform.class);
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
        mesh = null;
    }
}
