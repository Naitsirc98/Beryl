package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.rendering.ShadingModel;

public enum MaterialType {

    PHONG_MATERIAL(ShadingModel.PHONG_SHADING_MODEL),
    PBR_METALLIC_MATERIAL(ShadingModel.PBR_METALLIC_SHADING_MODEL),
    WATER_MATERIAL(ShadingModel.ANY_SHADING_MODEL);

    private final ShadingModel shadingModel;

    MaterialType(ShadingModel shadingModel) {
        this.shadingModel = shadingModel;
    }

    public ShadingModel getShadingModel() {
        return shadingModel;
    }
}
