package naitsirc98.beryl.scenes.components.behaviours;

import java.util.function.Consumer;

public final class MutableBehaviour extends Behaviour {

    private Stage onInit;
    private Stage onStart;
    private Stage onUpdate;
    private Stage onLateUpdate;
    private Stage onEnable;
    private Stage onDisable;
    private Stage onDestroy;

    private MutableBehaviour() {

    }

    public MutableBehaviour onInit(Stage onInit) {
        this.onInit = onInit;
        return this;
    }

    public MutableBehaviour onStart(Stage onStart) {
        this.onStart = onStart;
        return this;
    }

    public MutableBehaviour onUpdate(Stage onUpdate) {
        this.onUpdate = onUpdate;
        return this;
    }

    public MutableBehaviour onLateUpdate(Stage onLateUpdate) {
        this.onLateUpdate = onLateUpdate;
        return this;
    }

    public MutableBehaviour onEnable(Stage onEnable) {
        this.onEnable = onEnable;
        return this;
    }

    public MutableBehaviour onDisable(Stage onDisable) {
        this.onDisable = onDisable;
        return this;
    }

    public MutableBehaviour onDestroy(Stage onDestroy) {
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
    protected void onUpdate() {
        if(onUpdate != null) {
            onUpdate.accept(this);
        }
    }

    @Override
    protected void onLateUpdate() {
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

    public interface Stage extends Consumer<MutableBehaviour> {

    }
}
