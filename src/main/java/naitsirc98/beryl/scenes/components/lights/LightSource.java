package naitsirc98.beryl.scenes.components.lights;

import naitsirc98.beryl.lights.Light;
import naitsirc98.beryl.scenes.Component;

public final class LightSource extends Component<LightSource> {

    private Light<?> light;
    private boolean castShadows;

    private LightSource() {

    }

    public boolean castShadows() {
        return castShadows;
    }

    public LightSource castShadows(boolean castShadows) {
        this.castShadows = castShadows;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Light<T>> T light() {
        return (T) light;
    }

    public LightSource light(Light<?> light) {
        if(this.light != null && light == null) {
            disable();
        } else if(this.light == null && light != null) {
            enabled();
        }
        this.light = light;
        return this;
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    protected void onDestroy() {
        light = null;
    }

    @Override
    public Class<? extends Component> type() {
        return LightSource.class;
    }

    @Override
    protected LightSource self() {
        return this;
    }

}
