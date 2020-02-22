package naitsirc98.beryl.tasks;

import naitsirc98.beryl.logging.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

class TaskProcessor {

    private final ExecutorService threadPool;

    public TaskProcessor() {
        threadPool = Executors.newCachedThreadPool();
    }

    public void submit(Task task) {
        threadPool.submit(task);
    }

    public void shutdown() {
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, MILLISECONDS);
        } catch (InterruptedException e) {
            Log.error("Timeout error while waiting for TaskProcessor to shutdown", e);
        }
    }

}
