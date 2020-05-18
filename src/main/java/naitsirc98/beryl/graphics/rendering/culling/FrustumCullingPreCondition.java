package naitsirc98.beryl.graphics.rendering.culling;

import naitsirc98.beryl.meshes.views.MeshView;
import naitsirc98.beryl.scenes.components.meshes.MeshInstance;

public interface FrustumCullingPreCondition {

    FrustumCullingPreCondition NO_PRECONDITION = ((instance, meshView) -> FrustumCullingPreConditionState.CONTINUE);
    FrustumCullingPreCondition NEVER_PASS = ((instance, meshView) -> FrustumCullingPreConditionState.DISCARD);
    FrustumCullingPreCondition ALWAYS_PASS = ((instance, meshView) -> FrustumCullingPreConditionState.PASS);

    FrustumCullingPreConditionState getPrecondition(MeshInstance<?> instance, MeshView<?> meshView);
}
