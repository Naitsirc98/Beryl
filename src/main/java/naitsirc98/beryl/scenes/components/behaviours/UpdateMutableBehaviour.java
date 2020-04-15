package naitsirc98.beryl.scenes.components.behaviours;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class UpdateMutableBehaviour extends UpdateBehaviour {

    private Stage onInit;
    private Stage onStart;
    private Stage onUpdate;
    private Stage onEnable;
    private Stage onDisable;
    private Stage onDestroy;

    private Map<String, Object> variables;

    private UpdateMutableBehaviour() {

    }

    @Override
    protected void init() {
        variables = new HashMap<>();
        super.init();
    }

    public boolean exists(String variableName) {
        return variables.containsKey(variableName);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String variableName) {
        return (T) variables.get(variableName);
    }

    public void set(String variableName, Object value) {
        variables.put(variableName, value);
    }

    public void remove(String variableName) {
        variables.remove(variableName);
    }

    public void clearVariables() {
        variables.clear();
    }

    public UpdateMutableBehaviour onInit(Stage onInit) {
        this.onInit = onInit;
        return this;
    }

    public UpdateMutableBehaviour onStart(Stage onStart) {
        this.onStart = onStart;
        return this;
    }

    public UpdateMutableBehaviour onUpdate(Stage onUpdate) {
        this.onUpdate = onUpdate;
        return this;
    }

    public UpdateMutableBehaviour onEnable(Stage onEnable) {
        this.onEnable = onEnable;
        return this;
    }

    public UpdateMutableBehaviour onDisable(Stage onDisable) {
        this.onDisable = onDisable;
        return this;
    }

    public UpdateMutableBehaviour onDestroy(Stage onDestroy) {
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
    public void onUpdate() {
        if(onUpdate != null) {
            onUpdate.accept(this);
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

    public interface Stage extends Consumer<UpdateMutableBehaviour> {

    }
}
