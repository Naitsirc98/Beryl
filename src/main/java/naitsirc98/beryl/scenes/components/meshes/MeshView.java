package naitsirc98.beryl.scenes.components.meshes;

import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.scenes.Component;
import naitsirc98.beryl.scenes.components.math.Transform;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;

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

    public final Matrix4fc modelMatrix() {
        return requires(Transform.class).modelMatrix();
    }
    public final Matrix3fc normalMatrix() {
        return requires(Transform.class).normalMatrix();
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
