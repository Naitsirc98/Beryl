package naitsirc98.beryl.scenes;

import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.graphics.rendering.ShadingModel;

public class SceneRenderInfo {

    public static final ShadingModel DEFAULT_SHADING_MODEL = BerylConfiguration.DEFAULT_SHADING_MODEL.getOrDefault(ShadingModel.PHONG);
    private static final boolean SHADOWS_ENABLED_ON_START = BerylConfiguration.SHADOWS_ENABLED_ON_START.getOrDefault(true);


    private ShadingModel shadingModel;
    private boolean shadowsEnabled;

    public SceneRenderInfo() {
        shadingModel = DEFAULT_SHADING_MODEL;
        shadowsEnabled = SHADOWS_ENABLED_ON_START;
    }

    public ShadingModel getShadingModel() {
        return shadingModel;
    }

    public SceneRenderInfo setShadingModel(ShadingModel shadingModel) {
        this.shadingModel = shadingModel;
        return this;
    }

    public boolean areShadowsEnabled() {
        return shadowsEnabled;
    }

    public SceneRenderInfo setShadowsEnabled(boolean shadowsEnabled) {
        this.shadowsEnabled = shadowsEnabled;
        return this;
    }
}
