package naitsirc98.beryl.concurrency;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
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

    public State state() {
        return state.get();
    }

    public void start() {
        if(state.compareAndSet(State.IDLE, State.RUNNING)) {
            thread.submit(this::run);
        }
    }

    public void stop() {

    }

    public void pause() {

    }

    public void resume() {

    }

    private void run() {

        

    }

    public enum State {

        IDLE,
        RUNNING,
        PAUSED,
        TERMINATED

    }

}
