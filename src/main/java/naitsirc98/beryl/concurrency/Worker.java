package naitsirc98.beryl.concurrency;

import naitsirc98.beryl.logging.Log;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("ALL")
public class Worker {

    private static final long POP_TASK_TIMEOUT = 16;


    private final String name;
    private final Thread thread;
    private final BlockingQueue<Runnable> taskQueue;
    private final AtomicReference<State> state;
    private final Object sync = new Object();

    public Worker(String name) {
        this.name = name;
        thread = new Thread(this::run, name);
        taskQueue = new LinkedBlockingDeque<>();
        state = new AtomicReference<>(State.IDLE);
    }

    public String name() {
        return name;
    }

    public int taskCount() {
        return taskQueue.size();
    }

    public void submit(Runnable task) {
        if(state.get() == State.TERMINATED) {
            Log.error("This worker cannot accept tasks any longer because it is terminated");
        } else if(state.get() == State.AWAIT) {
            Log.error("Cannot submit a task while the worker is in the await state");
        } else {
            taskQueue.offer(requireNonNull(task));
        }
    }

    public State state() {
        return state.get();
    }

    public Worker start() {
        if(state.compareAndSet(State.IDLE, State.RUNNING)) {
            thread.start();
        }
        return this;
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

            synchronized(sync) {
                try {
                    sync.wait();
                } catch (InterruptedException e) {
                    Log.error("Timeout error while waiting for worker to finish", e);
                }
            }

            state.set(State.RUNNING);
        }
    }

    private void run() {

        while(state.get() != State.TERMINATED) {

            while(state.get() == State.PAUSED) {
                if(state.get() == State.TERMINATED) {
                    return;
                }
            }

            if(state.get() == State.AWAIT) {

                while(!taskQueue.isEmpty()) {
                    popTask().ifPresent(Runnable::run);
                }

                synchronized(sync) {
                    sync.notifyAll();
                }

                state.set(State.RUNNING);

            } else {
                popTask().ifPresent(Runnable::run);
            }

        }
    }

    private Optional<Runnable> popTask() {
        try {
            return Optional.ofNullable(taskQueue.poll(POP_TASK_TIMEOUT, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            Log.error("Exception while waiting for a worker task", e);
        }
        return Optional.empty();
    }

    private void shutdown() {
        await();
        joinThread();
        taskQueue.clear();
    }

    private void joinThread() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            Log.error("Timeout error while waiting for worker to complete the tasks", e);
        }
    }

    @Override
    public String toString() {
        return "Worker{" +
                "name='" + name + '\'' +
                ", tasks=" + taskQueue.size() +
                ", state=" + state +
                '}';
    }

    public enum State {

        IDLE,
        RUNNING,
        PAUSED,
        AWAIT,
        TERMINATED

    }

}
