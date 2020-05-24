package naitsirc98.beryl.scenes;

import naitsirc98.beryl.graphics.rendering.ShadingModel;

import static naitsirc98.beryl.core.BerylConfigConstants.SCENE_SHADING_MODEL;
import static naitsirc98.beryl.core.BerylConfigConstants.SHADOWS_ENABLED_ON_START;

public class SceneRenderInfo {

    private ShadingModel shadingModel;
    private boolean shadowsEnabled;

    public SceneRenderInfo() {
        shadingModel = SCENE_SHADING_MODEL;
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
