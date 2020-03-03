package naitsirc98.beryl.concurrency;

import naitsirc98.beryl.logging.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class Worker {

    private final ExecutorService thread;
    private final BlockingQueue<Runnable> taskQueue;
    private final AtomicReference<State> state;

    public Worker(String name) {
        thread = newSingleThreadExecutor();
        taskQueue = new LinkedBlockingDeque<>();
        state = new AtomicReference<>(State.IDLE);
    }

    public void submit(Runnable task) {
        if(state.get() == State.TERMINATED) {
            Log.error("This worker cannot accept tasks any longer because it is terminated");
        } else if(state.get() == State.AWAIT) {
            Log.error("Cannot submit a task while the worker is in the await state");
        } else {
            taskQueue.add(task);
        }
    }

    public State state() {
        return state.get();
    }

    public void start() {
        if(state.compareAndSet(State.IDLE, State.RUNNING)) {
            thread.submit(this::run);
        }
    }

    public void terminate() {
        if(state.get() != State.IDLE && state.get() != State.TERMINATED) {
            state.set(State.TERMINATED);
            shutdown();
        }
    }

    public void pause() {
        state.compareAndSet(State.RUNNING, State.PAUSED);
    }

    public void resume() {
        state.compareAndSet(State.PAUSED, State.RUNNING);
    }

    public void await() {
        if(state.compareAndSet(State.RUNNING, State.AWAIT)) {
            while(!taskQueue.isEmpty());
            state.set(State.RUNNING);
        }
    }

    private void run() {

        while(state.get() != State.TERMINATED) {

            while(state.get() == State.PAUSED);

            popTask().run();
        }
    }

    private Runnable popTask() {
        try {
            return taskQueue.take();
        } catch (InterruptedException e) {
            Log.error("Exception while waiting for a worker task", e);
        }
        return null;
    }

    private void shutdown() {
        thread.shutdownNow();
        waitForThread();
    }

    private void waitForThread() {
        try {
            thread.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.error("Timeout error while waiting for worker to complete the tasks", e);
        }
    }

    public enum State {

        IDLE,
        RUNNING,
        PAUSED,
        AWAIT,
        TERMINATED

    }

}
