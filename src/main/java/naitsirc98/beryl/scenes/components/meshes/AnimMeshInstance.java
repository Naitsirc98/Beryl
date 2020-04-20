package naitsirc98.beryl.scenes.components.meshes;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.meshes.views.AnimMeshView;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static naitsirc98.beryl.util.Asserts.assertTrue;

public class AnimMeshInstance extends MeshInstance<AnimMeshView> {

    @Override
    public Class<AnimMeshView> meshViewType() {
        return AnimMeshView.class;
    }

    public AnimMeshInstance meshViews(AnimMeshView... meshViews) {
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

    public AnimMeshInstance meshViews(Collection<AnimMeshView> meshViews) {
        if(this.meshViews != null) {
            Log.error("Cannot modify Mesh Instance component once you have set its Mesh Views");
        } else {
            assertNonNull(meshViews);
            assertTrue(meshViews.size() > 0);
            this.meshViews = meshViews.stream().filter(Objects::nonNull).distinct().collect(Collectors.toUnmodifiableList());
            doLater(() -> manager().enable(this));
        }
        return this;
    }
}
