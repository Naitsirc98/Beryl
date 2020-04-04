package naitsirc98.beryl.util.collections;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LookupTable<K, V> implements IDoubleMap<K, V> {

    public static <K1, V1> LookupTable<K1, V1> of(Map<K1, V1> map) {

        LookupTable<K1, V1> lookupTable = new LookupTable<>();

        map.forEach(lookupTable::put);

        return lookupTable;
    }

    private final Map<K, V> direct;
    private final Map<V, K> reverse;

    public LookupTable() {
        direct = new HashMap<>();
        reverse = new HashMap<>();
    }

    public void put(K key, V value) {
        direct.put(key, value);
        reverse.put(value, key);
    }

    @Override
    public boolean containsKey(K key) {
        return direct.containsKey(key);
    }

    @Override
    public boolean containsValue(V value) {
        return reverse.containsKey(value);
    }

    @Override
    public V valueOf(K key) {
        return direct.get(key);
    }

    @Override
    public K keyOf(V value) {
        return reverse.get(value);
    }

    public Collection<K> keys() {
        return direct.keySet();
    }

    public Collection<V> values() {
        return direct.values();
    }
}
