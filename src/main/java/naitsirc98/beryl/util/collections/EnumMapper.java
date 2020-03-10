package naitsirc98.beryl.util.collections;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static naitsirc98.beryl.util.Asserts.assertNonNull;

public class EnumMapper<K extends Enum<K>, V> implements IDoubleMap<K, V> {

    public static <K extends Enum<K>, V> EnumMapper<K, V> of(Class<K> enumClass, Function<K, V> valuesGenerator) {
        return new EnumMapper<>(enumClass, valuesGenerator);
    }

    public static <K extends Enum<K>, V> EnumMapper<K, V> of(EnumMap<K, V> enumMap) {
        return new EnumMapper<>(enumMap);
    }

    private final EnumMap<K, V> normalTable;
    private final Map<V, K> reversedTable;

    public EnumMapper(EnumMap<K, V> normalTable) {
        this.normalTable = normalTable;
        reversedTable = createReversedTable();
    }

    public EnumMapper(Class<K> enumClass, Function<K, V> valuesGenerator) {
        this.normalTable = createNormalTable(enumClass, valuesGenerator);
        this.reversedTable = createReversedTable();
    }

    @Override
    public boolean containsKey(K key) {
        return normalTable.containsKey(key);
    }

    @Override
    public boolean containsValue(V value) {
        return reversedTable.containsKey(value);
    }

    @Override
    public V valueOf(K key) {
        return normalTable.get(key);
    }

    @Override
    public K keyOf(V value) {
        return reversedTable.get(value);
    }

    private EnumMap<K, V> createNormalTable(Class<K> enumClass, Function<K, V> valuesGenerator) {

        EnumMap<K, V> table = new EnumMap<>(assertNonNull(enumClass));

        enumConstants(enumClass).forEach(enumConst -> table.put(enumConst, valuesGenerator.apply(enumConst)));

        return table;
    }

    private Map<V, K> createReversedTable() {

        Map<V, K> table = new HashMap<>(normalTable.size());

        normalTable.forEach((k, v) -> table.put(v, k));

        return table;
    }

    private Stream<K> enumConstants(Class<K> enumClass) {
        return EnumSet.allOf(enumClass).stream();
    }

}
