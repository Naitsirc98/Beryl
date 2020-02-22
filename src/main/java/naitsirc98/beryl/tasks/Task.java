package naitsirc98.beryl.tasks;

import naitsirc98.beryl.logging.Log;

import java.util.concurrent.atomic.AtomicReference;

public abstract class Task implements Runnable, Comparable<Task> {

    private final AtomicReference<State> state;

    protected Task() {
        state = new AtomicReference<>(State.NONE);
    }

    public final State state() {
        return state.get();
    }

    protected final void cancel() {
        if(state() != State.ENQUEUED) {
            Log.warning("Trying to cancel a task which is not enqueued");
        } else {
            state.set(State.CANCELED);
        }
    }

    @Override
    public final void run() {
        try {
            state.set(State.RUNNING);

            onStart();

            if(state() != State.CANCELED) {
                perform();
                onFinished();
                state.set(State.SUCCESS);
            }
        } catch(Throwable e) {
            state.set(State.FAILURE);
            Log.error("Error while executing task " + toString(), e);
            onError(e);
        }
    }

    protected void onStart() {

    }

    protected void perform() {

    }

    protected void onFinished() {

    }

    protected void onError(Throwable error) {

    }

    public int priority() {
        return 0;
    }

    @Override
    public final int compareTo(Task other) {
        return Integer.compare(priority(), other.priority());
    }

    final void state(State state) {
        this.state.set(state);
    }

    public boolean notCanceled() {
        return state() != State.CANCELED;
    }

    public enum State {

        NONE,
        ENQUEUED,
        CANCELED,
        RUNNING,
        FAILURE,
        SUCCESS

    }
}
