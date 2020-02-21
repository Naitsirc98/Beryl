package naitsirc98.beryl.scenes;

public abstract class Component<SELF extends Component> extends SceneObject {

    Entity entity;
    private ComponentManager<SELF> system;
    private boolean enabled;
    private boolean destroyed;

    public Entity entity() {
        return entity;
    }

    @Override
    public Scene scene() {
        return entity().scene();
    }

    @Override
    public boolean enabled() {
        return entity.enabled() && enabled;
    }

    @Override
    public final SELF enable() {
        if(!enabled()) {
            // TODO: set enabled to its system
        }
        return self();
    }

    @Override
    public final SELF disable() {
        if(enabled()) {
            // TODO: set disabled to its system
        }
        return self();
    }

    @Override
    public final boolean destroyed() {
        return destroyed;
    }

    public Class<? extends Component> type() {
        return getClass();
    }

    protected abstract void onEnable();

    protected abstract void onDisable();

    protected abstract SELF self();
}
