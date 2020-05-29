package naitsirc98.beryl.graphics.opengl;

import naitsirc98.beryl.resources.ManagedResource;
import naitsirc98.beryl.resources.Resource;
import naitsirc98.beryl.util.handles.IntHandle;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public abstract class GLObject extends ManagedResource implements IntHandle, Resource {

    private final GLContext context;
    private int handle;
    private String name;

    public GLObject(GLContext context, int handle) {
        this.context = requireNonNull(context);
        this.handle = handle;
        name = "";
    }

    @Override
    public final int handle() {
        return handle;
    }

    public String name() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public <T extends GLObject> T name(String name) {
        this.name = name;
        return (T) this;
    }

    public final GLContext context() {
        return context;
    }

    public final GLMapper mapper() {
        return context.mapper();
    }

    protected final <T> T mapToAPI(Object obj) {
        return mapper().mapToAPI(obj);
    }

    protected final <T> T mapFromAPI(Class<T> clazz, int apiEnum) {
        return mapper().mapFromAPI(clazz, apiEnum);
    }

    protected final void setHandle(int handle) {
        this.handle = handle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GLObject glObject = (GLObject) o;
        return handle == glObject.handle;
    }

    @Override
    public int hashCode() {
        return Objects.hash(handle);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                " name=" + name +
                ", handle='" + handle + '\'' +
                '}';
    }
}
