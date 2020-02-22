package naitsirc98.beryl.tasks;

import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.Singleton;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * The Task Manager enqueues and asynchronously executes tasks
 */
public final class TaskManager extends BerylSystem {

    private static final int TASK_POP_TIMEOUT = 16;

    @Singleton
    private static TaskManager instance;


    /**
     * Submit a task to be executed in the future.
     *
     * @param task the task
     */
    public static void submitTask(Task task) {
        instance.submit(task);
    }

    /**
     * Returns the number of enqueued tasks.
     *
     * @return the number of tasks
     */
    public static int taskCount() {
        return instance.taskQueue.size();
    }


    private final AtomicBoolean running;
    private final BlockingQueue<Task> taskQueue;
    private final ExecutorService taskThread;
    private final TaskProcessor taskProcessor;

    private TaskManager() {
        running = new AtomicBoolean(false);
        taskQueue = new PriorityBlockingQueue<>();
        taskThread = Executors.newSingleThreadExecutor();
        taskProcessor = new TaskProcessor();
    }

    @Override
    protected void init() {
        running.set(true);
        taskThread.submit(this::run);
    }

    @Override
    protected void terminate() {

        running.set(false);
        taskProcessor.shutdown();
        taskThread.shutdown();

        Log.info("[TASK-MANAGER]: waiting for " + taskCount() + " tasks to complete...");

        try {
            taskThread.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.error("Timeout error while waiting for TaskManager to terminate", e);
        }
    }

    private void submit(Task task) {

        if(task == null) {
            Log.error("Cannot submit a null task");
            return;
        }

        if(task.state() == Task.State.CANCELED) {
            return;
        }

        taskQueue.add(task);
    }

    private void run() {
        while(running.get()) {
            while(!taskQueue.isEmpty()) {
                popTask().filter(Task::notCanceled).ifPresent(taskProcessor::submit);
            }
        }
    }

    private Optional<Task> popTask() {
        try {
            return Optional.ofNullable(taskQueue.poll(TASK_POP_TIMEOUT, MILLISECONDS));
        } catch (InterruptedException e) {
            Log.error("Timeout error while polling the task queue", e);
        }
        return Optional.empty();
    }


}
