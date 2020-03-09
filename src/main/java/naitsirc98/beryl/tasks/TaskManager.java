package naitsirc98.beryl.tasks;

import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.types.Singleton;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * The Task Manager enqueues and asynchronously executes tasks
 */
public final class TaskManager extends BerylSystem {

    private static final int TASK_POP_TIMEOUT = 16;

    private static final Task TERMINATION_TASK = new TerminationTask();

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
        taskThread = newSingleThreadExecutor(runnable -> {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setName("TaskManager Thread");
            thread.setDaemon(true);
            return thread;
        });
        taskProcessor = new TaskProcessor();
    }

    @Override
    protected void init() {
        running.set(true);
        taskThread.submit(this::run);
    }

    @Override
    protected void terminate() {

        taskQueue.add(TERMINATION_TASK);
        running.set(false);
        taskProcessor.shutdown();
        taskThread.shutdown();

        Log.info("[TASK-MANAGER]: Waiting for " + taskCount() + " tasks to complete...");

        try {
            taskThread.awaitTermination(Long.MAX_VALUE, MILLISECONDS);
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

                final Task task = popTask();

                if(task == TERMINATION_TASK) {
                    return;
                }

                if(task != null && task.state() != Task.State.CANCELED) {
                    taskProcessor.submit(task);
                }
            }
        }
    }

    private Task popTask() {
        try {
            return taskQueue.take();
        } catch (InterruptedException e) {
            Log.error("TaskManager thread has been interrupted", e);
        }
        return null;
    }


    private static final class TerminationTask extends Task {
        @Override
        protected void perform() {

        }
    }
}
