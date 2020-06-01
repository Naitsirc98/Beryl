package naitsirc98.beryl.util.collections;

import java.util.*;
import java.util.stream.Stream;

public class FastIterableSet<T> implements Set<T> {

    private T[] data;
    private final Queue<Integer> freeIndices;
    private final Map<Object, Integer> indexTable;
    private int size;

    public FastIterableSet() {
        this(16);
    }

    @SuppressWarnings("unchecked")
    public FastIterableSet(int initialCapacity) {
        data = (T[]) new Object[initialCapacity];
        freeIndices = new ArrayDeque<>(initialCapacity);
        indexTable = new HashMap<>(initialCapacity);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return indexTable.containsKey(o);
    }

    @Override
    public Object[] toArray() {
        return data;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T1> T1[] toArray(T1[] a) {
        System.arraycopy((T1[]) data, 0, a, 0, a.length);
        return a;
    }

    @Override
    public boolean add(T t) {

        if(indexTable.containsKey(t)) {
            return false;
        }

        if(!freeIndices.isEmpty()) {

            final int index = freeIndices.poll();
            data[index] = t;
            indexTable.put(t, index);

        } else {

            if(size >= data.length) {
                data = Arrays.copyOf(data, Math.round(size * 1.5f));
            }

            indexTable.put(t, size);
            data[size] = t;
        }

        ++size;

        return true;
    }

    @Override
    public boolean remove(Object o) {

        if(!indexTable.containsKey(o)) {
            return false;
        }

        final int index = indexTable.get(o);
        data[index] = null;
        freeIndices.add(index);
        indexTable.remove(o);

        --size;

        return true;
    }

    @Override
    public void clear() {
        size = 0;
        Arrays.fill(data, null);
        freeIndices.clear();
        indexTable.clear();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean allAdded = false;
        for(T t : c) {
            allAdded |= add(t);
        }
        return allAdded;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean allRemoved = false;
        for(Object o : c) {
            allRemoved |= remove(o);
        }
        return allRemoved;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Iterator<T> it = iterator();
        boolean changed = false;
        while(it.hasNext()) {
            T t = it.next();
            if(!c.contains(t)) {
                it.remove();
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().allMatch(this::contains);
    }

    @Override
    public Stream<T> stream() {
        return Arrays.stream(data).unordered().filter(Objects::nonNull);
    }

    @Override
    public Stream<T> parallelStream() {
        return Arrays.stream(data).parallel().unordered().filter(Objects::nonNull);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FastIterableSet<?> that = (FastIterableSet<?>) o;
        return Arrays.equals(data, that.data) &&
                Objects.equals(freeIndices, that.freeIndices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, freeIndices);
    }

    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}
