package naitsirc98.beryl.scenes;

/**
 * The base class for all the objects that life in a scene
 */
public abstract class SceneObject {

    private boolean destroyed;

    /**
     * The scene of this object
     *
     * @return the scene
     */
    public abstract Scene scene();

    /**
     * Tells whether this object is enabled or not. If an object is enabled, then it will be updated
     *
     * @return true if this object is enabled, false otherwise
     */
    public abstract boolean enabled();

    /**
     * Enables this object
     *
     * @return this object
     */
    public abstract SceneObject enable();

    /**
     * Disables this object
     *
     * @return this object
     */
    public abstract SceneObject disable();

    /**
     * Tells whether this object is destroyed, or not. It does not necessarily mean that it is completely removed from the scene,
     * but that this object is going to be destroyed soon.
     *
     * @return true if this object is destroyed (or is going to be destroyed), false otherwise
     */
    public final boolean destroyed() {
        return destroyed;
    }

    /**
     * Marks this object as destroyed. It will be destroyed as soon as possible.
     */
    final void markDestroyed() {
        destroyed = true;
    }

    /**
     * Destroys this object in the next destroy pass
     */
    public abstract void destroy();

    /**
     * Destroys this object immediately. This use is highly discouraged, call {@link SceneObject#destroy()} instead
     */
    public abstract void destroyNow();

    /**
     * This gets called whenever this object is going to be finally destroyed. You should release resources here.
     */
    protected abstract void onDestroy();

    /**
     * Performs a task in the next task processing pass.
     *
     * @param task the task to be executed
     */
    protected final void doLater(Runnable task) {
        scene().submit(task);
    }
}
