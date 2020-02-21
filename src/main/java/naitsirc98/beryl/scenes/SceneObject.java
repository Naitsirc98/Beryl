package naitsirc98.beryl.scenes;

public abstract class SceneObject {

    private boolean destroyed;

    public abstract Scene scene();

    public abstract boolean enabled();

    public abstract SceneObject enable();

    public abstract SceneObject disable();

    public final boolean destroyed() {
        return destroyed;
    }

    final void markDestroyed() {
        destroyed = true;
    }

    public abstract void destroy();

    public abstract void destroyNow();

    protected abstract void onDestroy();

    protected final void doLater(Runnable task) {
        scene().submit(task);
    }
}
