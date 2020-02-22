package naitsirc98.beryl.scenes.components.behaviours;

import naitsirc98.beryl.scenes.Component;

public class Behaviour extends Component<Behaviour> {

    private boolean started;

    @Override
    protected final void init() {
        super.init();
        started = false;
    }

    public boolean started() {
        return started;
    }

    void start() {
        onStart();
        started = true;
    }

    protected void onStart() {

    }

    protected void onUpdate() {

    }

    protected void onLateUpdate() {

    }

    @Override
    public Class<? extends Component> type() {
        return Behaviour.class;
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    protected final Behaviour self() {
        return this;
    }

    @Override
    protected void onDestroy() {

    }

}
