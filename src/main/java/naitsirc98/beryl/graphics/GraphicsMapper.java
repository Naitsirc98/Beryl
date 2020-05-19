package naitsirc98.beryl.graphics;

import naitsirc98.beryl.util.collections.IDoubleMap;

import java.util.HashMap;
import java.util.Map;

public abstract class GraphicsMapper {

    private final Map<Class<?>, IDoubleMap<Object, Object>> mappers;

    public GraphicsMapper() {
        this.mappers = new HashMap<>();
        init();
    }

    @SuppressWarnings("unchecked")
    public <T> T mapFromAPI(Class<T> clazz, Object apiEnum) {

        if(!mappers.containsKey(clazz)) {
            throw new IllegalArgumentException("There is no mapper for class " + clazz.getSimpleName());
        }

        final IDoubleMap<Object, Object> mapper = mappers.get(clazz);

        if(!mapper.containsValue(apiEnum)) {
            throw new IllegalArgumentException("Unable to convert " + apiEnum + " to an object of class " + clazz.getSimpleName());
        }

        return (T) mapper.keyOf(apiEnum);
    }

    @SuppressWarnings("unchecked")
    public <T> T mapToAPI(Object key) {

        if(!mappers.containsKey(key.getClass())) {
            throw new IllegalArgumentException("There is no mapper for class " + key.getClass().getSimpleName());
        }

        final IDoubleMap<Object, Object> mapper = mappers.get(key.getClass());

        if(!mapper.containsKey(key)) {
            throw new IllegalArgumentException("Unknown enum key " + key);
        }

        return (T) mapper.valueOf(key);
    }

    protected abstract void init();

    @SuppressWarnings("unchecked")
    protected <K> void register(Class<K> clazz, IDoubleMap<K, ?> mapper) {
        mappers.put(clazz, (IDoubleMap<Object, Object>) mapper);
    }
}
