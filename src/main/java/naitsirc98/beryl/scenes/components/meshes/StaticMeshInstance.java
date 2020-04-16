package naitsirc98.beryl.scenes.components.meshes;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.meshes.views.StaticMeshView;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static naitsirc98.beryl.util.Asserts.assertTrue;

public class StaticMeshInstance extends MeshInstance<StaticMeshView> {

    @Override
    public Class<StaticMeshView> meshViewType() {
        return StaticMeshView.class;
    }

    public StaticMeshInstance meshViews(StaticMeshView... meshViews) {
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
}