package naitsirc98.beryl.concurrency;

import java.util.Arrays;

public class FixedWorkerPool implements WorkerPool {

    private final String name;
    private final Worker[] workers;
    private int workerIndex;

    public FixedWorkerPool(String name, int workerCount) {
        this.name = name;
        workers = new Worker[workerCount];
        for(int i = 0;i < workerCount;i++) {
            workers[i] = new Worker(name + "-Worker[" + i + "]").start();
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int taskCount() {
        return Arrays.stream(workers).mapToInt(Worker::taskCount).sum();
    }

    @Override
    public int workerCount() {
        return workers.length;
    }

    @Override
    public void submit(Runnable task) {
        nextWorker().submit(task);
    }

    @Override
    public void submit(Iterable<Runnable> tasks) {
        for(Runnable task : tasks) {
            nextWorker().submit(task);
        }
    }

    @Override
    public void pause() {
        for(Worker worker : workers) {
            worker.pause();
        }
    }

    @Override
    public void resume() {
        for(Worker worker : workers) {
            worker.resume();
        }
    }

    @Override
    public void await() {
        for(Worker worker : workers) {
            worker.await();
        }
    }

    @Override
    public void terminate() {
        for(Worker worker : workers) {
            worker.terminate();
        }
    }

    @Override
    public String toString() {
        return "FixedWorkerPool{" +
                "name='" + name + '\'' +
                ", workers=" + Arrays.toString(workers) +
                '}';
    }

    private Worker nextWorker() {
        final Worker worker = workers[workerIndex++];
        if(workerIndex >= workerCount()) {
            workerIndex = 0;
        }
        return worker;
    }
}
