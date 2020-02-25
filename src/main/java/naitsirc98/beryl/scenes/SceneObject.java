package naitsirc98.beryl.scenes;

import naitsirc98.beryl.logging.Log;

import static naitsirc98.beryl.util.Asserts.ASSERTS_ENABLED;

/**
 * The base class for all the objects that live in a scene
 */
public abstract class SceneObject {

    private static final byte MARK_DESTROYED = 1;
    private static final byte DELETED = 2;

    private byte destroyState;

    /**
     * Initializes this SceneObject
     *
     * */
    void init() {
        destroyState = 0;
    }

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
     * Tells whether this object is destroyed, or not. It means that it will be deleted and removed from the scene in the next
     * task processing pass.
     *
     * @return true if this object is marked as destroyed, false otherwise
     */
    public boolean destroyed() {
        return destroyState == MARK_DESTROYED;
    }

    /***
     * Tells whether this object has been totally destroyed or not. An object is deleted if it is removed from
     * the scene and {@link SceneObject#onDestroy()} has been called.
     *
     * A deleted scene object is considered to be a {@code null} reference, so it is not usable anymore.
     *
     * @return true if this object is completely destroyed, false otherwise
     */
    public boolean deleted() {
        return destroyState == DELETED;
    }

    /**
     * Deletes this object. This will call the {@link SceneObject#onDestroy()} method.
     *
     *
     * */
    void delete() {
        onDestroy();
        destroyState = DELETED;
    }

    /**
     * Marks this object as destroyed. It will be destroyed as soon as possible.
     */
    final void markDestroyed() {
        destroyState = MARK_DESTROYED;
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
        assertNotDeleted();
        scene().submit(task);
    }

    protected final void assertNotDeleted() {
        if(ASSERTS_ENABLED) {
            if(deleted()) {
                Log.fatal("SceneObject " + toString() + " is deleted");
            }
        }
    }
}
