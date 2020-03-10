package naitsirc98.beryl.util.collections;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class OptimizedArray<T> implements List<T> {

    private final ArrayList<T> list;
    private final Queue<Integer> freeIndices;

    public OptimizedArray() {
        this(16);
    }

    public OptimizedArray(int initialCapacity) {
        list = new ArrayList<>(initialCapacity);
        freeIndices = new ArrayDeque<>();
    }

    public void trimToSize() {

        while(!freeIndices.isEmpty()) {
            remove(freeIndices.poll().intValue());
        }

        list.trimToSize();
    }

    @Override
    public int size() {
        return list.size() - freeIndices.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return o != null && list.contains(o);
    }

    @Override
    public int indexOf(Object o) {
        return o == null ? -1 : list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return o == null ? -1 : list.lastIndexOf(o);
    }

    @Override
    public Object[] toArray() {
        return list.stream().filter(Objects::nonNull).toArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T1> T1[] toArray(T1[] a) {

        final int size = size();

        if(a.length < size) {
            return (T1[]) toArray();
        }

        for(int i = 0;i < size;i++) {
            a[i] = (T1) get(i);
        }

        return a;
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public T set(int index, T element) {
        requireNonNull(element);
        freeIndices.remove(index);
        return list.set(index, element);
    }

    @Override
    public boolean add(T t) {
        requireNonNull(t);

        if(!freeIndices.isEmpty()) {
            list.set(freeIndices.poll(), t);
        } else {
            list.add(t);
        }

        return true;
    }

    @Override
    public void add(int index, T element) {
        requireNonNull(element);
        freeIndices.remove(index);
        list.add(index, element);
    }

    @Override
    public T remove(int index) {
        final T old = list.set(index, null);
        freeIndices.add(index);
        return old;
    }

    @Override
    public boolean remove(Object o) {
        final int index = indexOf(o);
        if(index >= 0) {
            list.set(index, null);
            freeIndices.add(index);
        }
        return index >= 0;
    }

    @Override
    public void clear() {
        list.clear();
        freeIndices.clear();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return list.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public ListIterator<T> listIterator() {
        return list.listIterator();
    }

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public Stream<T> stream() {
        return list.stream().filter(Objects::nonNull);
    }

    @Override
    public Stream<T> parallelStream() {
        return list.parallelStream().filter(Objects::nonNull);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OptimizedArray<?> that = (OptimizedArray<?>) o;
        return Objects.equals(list, that.list) &&
                Objects.equals(freeIndices, that.freeIndices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list, freeIndices);
    }

    @Override
    public String toString() {
        return list.toString();
    }
}
