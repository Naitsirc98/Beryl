package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.materials.IMaterial;
import naitsirc98.beryl.meshes.views.MeshView;

public interface MeshViewFactory<T, U extends MeshView> {

    U create(T key, IMaterial material);

}
