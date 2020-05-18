package naitsirc98.beryl.graphics.rendering.culling;

import naitsirc98.beryl.scenes.components.meshes.MeshInstanceList;
import org.joml.FrustumIntersection;

public interface FrustumCuller {

    void init();

    void terminate();

    int performCullingCPU(FrustumIntersection frustum, MeshInstanceList<?> instances);

    int performCullingCPU(FrustumIntersection frustum, MeshInstanceList<?> instances, FrustumCullingPreCondition preCondition);

}
