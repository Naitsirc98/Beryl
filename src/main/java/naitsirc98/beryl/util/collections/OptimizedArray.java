package naitsirc98.beryl.util.collections;

import java.util.*;
import java.util.stream.Stream;

public class OptimizedArray<T> implements Set<T> {

    private T[] array;
    private final Queue<Integer> freeIndices;
    private final Map<Object, Integer> indexTable;
    private int size;

    public OptimizedArray() {
        this(16);
    }

    @SuppressWarnings("unchecked")
    public OptimizedArray(int initialCapacity) {
        array = (T[]) new Object[initialCapacity];
        freeIndices = new ArrayDeque<>();
        indexTable = new HashMap<>();
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
        return array;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T1> T1[] toArray(T1[] a) {
        System.arraycopy((T1[])array, 0, a, 0, a.length);
        return a;
    }

    @Override
    public boolean add(T t) {

        if(indexTable.containsKey(t)) {
            return false;
        }

        if(!freeIndices.isEmpty()) {

            final int index = freeIndices.poll();
            array[index] = t;
            indexTable.put(t, index);

        } else {

            if(size >= array.length) {
                array = Arrays.copyOf(array, Math.round(size * 1.5f));
            }

            indexTable.put(t, size);
            array[size] = t;
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
        array[index] = null;
        freeIndices.add(index);
        indexTable.remove(o);

        --size;

        return true;
    }

    public void trim(int newSize) {
        if(size == newSize) {
            return;
        }
        array = Arrays.copyOf(array, newSize);
        size = Math.min(newSize, size);
    }

    @Override
    public void clear() {
        size = 0;
        Arrays.fill(array, null);
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
        return Arrays.stream(array).unordered().filter(Objects::nonNull);
    }

    @Override
    public Stream<T> parallelStream() {
        return Arrays.stream(array).parallel().unordered().filter(Objects::nonNull);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OptimizedArray<?> that = (OptimizedArray<?>) o;
        return Arrays.equals(array, that.array) &&
                Objects.equals(freeIndices, that.freeIndices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(array, freeIndices);
    }

    @Override
    public String toString() {
        return Arrays.toString(array);
    }
}
