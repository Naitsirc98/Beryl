package naitsirc98.beryl.util.collections;

public interface IDoubleMap<K, V> {

    boolean containsKey(K key);

    boolean containsValue(V value);

    V valueOf(K key);

    K keyOf(V value);

}
