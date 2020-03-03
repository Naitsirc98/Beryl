package naitsirc98.beryl.concurrency;

public interface WorkerPool {

    String name();

    int taskCount();

    int workerCount();

    void submit(Runnable task);

    void submit(Iterable<Runnable> task);

    void pause();

    void resume();

    void await();

    void terminate();
}
