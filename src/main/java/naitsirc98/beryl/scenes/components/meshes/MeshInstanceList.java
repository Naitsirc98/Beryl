package naitsirc98.beryl.scenes.components.meshes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public final class MeshInstanceList<T extends MeshInstance> implements Iterable<T> {

    private final List<T> instances;
    private int numMeshViews;

    public MeshInstanceList() {
        instances = new ArrayList<>();
        numMeshViews = 0;
    }

    public boolean empty() {
        return instances.isEmpty();
    }

    public int size() {
        return instances.size();
    }

    public int numMeshViews() {
        return numMeshViews;
    }

    public void add(T instance) {
        instances.add(instance);
        numMeshViews += instance.numMeshViews();
    }

    public void remove(T instance) {
        instances.remove(instance);
        numMeshViews -= instance.numMeshViews();
    }

    public void clear() {
        instances.clear();
        numMeshViews = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return instances.iterator();
    }

    public Stream<T> stream() {
        return instances.stream();
    }
}
