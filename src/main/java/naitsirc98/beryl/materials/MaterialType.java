package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.rendering.ShadingModel;

public enum MaterialType {

    PHONG_MATERIAL(ShadingModel.PHONG),
    PBR_METALLIC_MATERIAL(ShadingModel.PBR_METALLIC),
    WATER_MATERIAL(null);

    private final ShadingModel shadingModel;

    MaterialType(ShadingModel shadingModel) {
        this.shadingModel = shadingModel;
    }

    public ShadingModel getShadingModel() {
        return shadingModel;
    }
}
