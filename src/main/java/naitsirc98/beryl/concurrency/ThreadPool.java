package naitsirc98.beryl.concurrency;

import java.util.ArrayList;
import java.util.List;

public class ThreadPool {

    private static final int INITIAL_CAPACITY = 2;

    private final List<Worker> workers;

    public ThreadPool() {
        this(INITIAL_CAPACITY);
    }

    public ThreadPool(int initialCapacity) {
        workers = new ArrayList<>(initialCapacity);
    }

}
