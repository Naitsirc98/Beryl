package naitsirc98.beryl.util.collections;

import java.util.HashMap;
import java.util.Map;

public interface IDoubleMap<K, V> {

    static <K1, V1> IDoubleMap<K1, V1> of(Map<K1, V1> map) {
        return new SimpleDoubleMap<>(map);
    }

    boolean containsKey(K key);

    boolean containsValue(V value);

    V valueOf(K key);

    K keyOf(V value);

    class SimpleDoubleMap<K1, V1> implements IDoubleMap<K1, V1> {

        private final Map<K1, V1> normalMap;
        private final Map<V1, K1> reversedMap;

        public SimpleDoubleMap(Map<K1, V1> map) {
            this.normalMap = map;
            reversedMap = createReversedMap();
        }

        @Override
        public boolean containsKey(K1 key) {
            return normalMap.containsKey(key);
        }

        @Override
        public boolean containsValue(V1 value) {
            return reversedMap.containsKey(value);
        }

        @Override
        public V1 valueOf(K1 key) {
            return normalMap.get(key);
        }

        @Override
        public K1 keyOf(V1 value) {
            return reversedMap.get(value);
        }

        private Map<V1, K1> createReversedMap() {

            Map<V1, K1> table = new HashMap<>(normalMap.size());

            normalMap.forEach((k, v) -> table.put(v, k));

            return table;
        }
    }
}
