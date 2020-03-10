package naitsirc98.beryl.scenes.components.behaviours;

import java.util.function.Consumer;

public final class LateMutableBehaviour extends LateBehaviour {

    private Stage onInit;
    private Stage onStart;
    private Stage onLateUpdate;
    private Stage onEnable;
    private Stage onDisable;
    private Stage onDestroy;

    private LateMutableBehaviour() {

    }

    public LateMutableBehaviour onInit(Stage onInit) {
        this.onInit = onInit;
        return this;
    }

    public LateMutableBehaviour onStart(Stage onStart) {
        this.onStart = onStart;
        return this;
    }

    public LateMutableBehaviour onLateUpdate(Stage onLateUpdate) {
        this.onLateUpdate = onLateUpdate;
        return this;
    }

    public LateMutableBehaviour onEnable(Stage onEnable) {
        this.onEnable = onEnable;
        return this;
    }

    public LateMutableBehaviour onDisable(Stage onDisable) {
        this.onDisable = onDisable;
        return this;
    }

    public LateMutableBehaviour onDestroy(Stage onDestroy) {
        this.onDestroy = onDestroy;
        return this;
    }

    @Override
    protected void onInit() {
        if(onInit != null) {
            onInit.accept(this);
        }
    }

    @Override
    protected void onStart() {
        if(onStart != null) {
            onStart.accept(this);
        }
    }

    @Override
    public void onLateUpdate() {
        if(onLateUpdate != null) {
            onLateUpdate.accept(this);
        }
    }

    @Override
    protected void onEnable() {
        if(onEnable != null) {
            onEnable.accept(this);
        }
    }

    @Override
    protected void onDisable() {
        if(onDisable != null) {
            onDisable.accept(this);
        }
    }

    @Override
    protected void onDestroy() {
        if(onDestroy != null) {
            onDestroy.accept(this);
        }
    }

    public interface Stage extends Consumer<LateMutableBehaviour> {

    }
}
