package naitsirc98.beryl.scenes;

public abstract class Component<SELF extends Component> extends SceneObject {

    Entity entity;
    ComponentManager<SELF> system;
    private boolean enabled;

    public Entity entity() {
        return entity;
    }

    protected final <T extends Component> T requires(Class<T> componentClass) {
        return entity.requires(componentClass);
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

    public Class<? extends Component> type() {
        return getClass();
    }

    @Override
    public final void destroy() {
        entity.destroy(this);
    }

    @Override
    public void destroyNow() {
        entity.destroyNow(this);
    }

    protected abstract void onEnable();

    protected abstract void onDisable();

    protected abstract SELF self();
}
