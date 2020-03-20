package naitsirc98.beryl.graphics.opengl;

import naitsirc98.beryl.resources.Resource;
import naitsirc98.beryl.util.handles.IntHandle;

import static naitsirc98.beryl.graphics.Graphics.opengl;

public interface GLObject extends IntHandle, Resource {

    default GLMapper mapper() {
        return opengl().mapper();
    }

    default <T> T mapToAPI(Object obj) {
        return mapper().mapToAPI(obj);
    }

    default <T> T mapFromAPI(Class<T> clazz, int apiEnum) {
        return mapper().mapFromAPI(clazz, apiEnum);
    }

}
