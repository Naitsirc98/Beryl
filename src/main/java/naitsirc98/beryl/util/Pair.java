package naitsirc98.beryl.util;

import java.util.function.BiConsumer;

public final class Pair<K, V> {

    private final K first;
    private final V second;

    public Pair(K first, V second) {
        this.first = first;
        this.second = second;
    }

    public K first() {
        return first;
    }

    public V second() {
        return second;
    }

    public void get(BiConsumer<K, V> consumer) {
        consumer.accept(first, second);
    }

}
