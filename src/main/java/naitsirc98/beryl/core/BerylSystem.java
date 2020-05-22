package naitsirc98.beryl.core;

/**
 * Base class for all Beryl Systems. A Beryl system is an important module of the engine that must be initialized at the
 * very start and terminated when the application exits.
 *
 * A Beryl System may be dependent of others. If that's the case, it will be loaded after the dependencies, and it will be
 * terminated before those. Cyclic dependencies are illegal.
 *
 * If a {@link BerylSystem} fails to initialize, the application will not start.
 */
public abstract class BerylSystem {

    private final BerylSystemManager systemManager;
    private boolean initialized;

    public BerylSystem(BerylSystemManager systemManager) {
        this.systemManager = systemManager;
    }

    /**
     * Gets this BerylSystem Manager
     * */
    protected final BerylSystemManager getSystemManager() {
        return systemManager;
    }

    /**
     * Marks this system as successfully initialized.
     */
    final void markInitialized() {
        initialized = true;
    }

    /**
     * Tells whether this system has been initialized or not.
     *
     * @return true if this system was initialized, false otherwise
     */
    public final boolean initialized() {
        return initialized;
    }

    /**
     * Initialize this system.
     */
    protected abstract void init();

    /**
     * Terminates this system.
     */
    protected abstract void terminate();

    /**
     * Returns the debug report of this system.
     *
     * @return the debug report.
     */
    protected CharSequence debugReport() {
        return null;
    }

}
