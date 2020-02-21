package naitsirc98.beryl.scenes;

public abstract class ComponentManager<T extends Component> {

    Scene scene;

    public final Scene scene() {
        return scene;
    }

    protected abstract void init();

    protected abstract void addEnabled(T component);

    protected abstract void addDisabled(T component);

    protected abstract void enable(T component);

    protected abstract void disable(T component);

    protected abstract void destroy(T component);

    protected abstract void destroy();
}
