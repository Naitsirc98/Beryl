package naitsirc98.beryl.scenes;

public abstract class SceneObject {

    public abstract Scene scene();

    public abstract boolean enabled();

    public abstract SceneObject enable();

    public abstract SceneObject disable();

    public abstract boolean destroyed();

    public abstract void destroy();

    protected abstract void onDestroy();
}
