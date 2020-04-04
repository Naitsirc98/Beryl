package naitsirc98.beryl.resources;

public abstract class ManagedResource implements Resource {

    private boolean released;

    protected ManagedResource() {
        this(true);
    }

    protected ManagedResource(boolean track) {
        if(track) {
            ResourceManager.track(this);
        }
    }

    @Override
    public boolean released() {
        return released;
    }

    public boolean tracked() {
        return ResourceManager.tracked(this);
    }

    public void track() {
        ResourceManager.track(this);
    }

    public void untrack() {
        ResourceManager.untrack(this);
    }

    @Override
    public synchronized final void release() {
        if(releaseNoUntrack()) {
            untrack();
        }
    }

    final boolean releaseNoUntrack() {
        if(!released) {
            free();
            released = true;
            return true;
        }
        return false;
    }

    protected abstract void free();
}
