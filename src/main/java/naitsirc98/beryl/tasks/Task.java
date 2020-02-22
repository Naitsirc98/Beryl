package naitsirc98.beryl.tasks;

import naitsirc98.beryl.logging.Log;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Base class for all asynchronous tasks. They are managed by the {@link TaskManager}
 */
public abstract class Task implements Runnable, Comparable<Task> {

    private final AtomicReference<State> state;

    /**
     * Instantiates a new Task.
     */
    protected Task() {
        state = new AtomicReference<>(State.NONE);
    }

    /**
     * Returns the current state of this task.
     *
     * @return the current state
     */
    public final State state() {
        return state.get();
    }

    /**
     * Cancels the execution of this task. It only works if the task has not entered the {@link Task#perform} method.
     *
     * Should be used when the task is enqueued, but before being executed.
     *
     */
    protected final void cancel() {
        if(state() != State.ENQUEUED) {
            Log.warning("Trying to cancel a task which is not enqueued");
        } else {
            state.set(State.CANCELED);
        }
    }

    /**
     * Executes this task
     *
     * */
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

    /**
     * Gets called just before {@link Task#perform()} method. Could be canceled at this point to not continue the execution.
     */
    protected void onStart() {

    }

    /**
     * Performs the task
     */
    protected abstract void perform();

    /**
     * Gets called whenever the task has successfully finished execution.
     */
    protected void onFinished() {

    }

    /**
     * Gets called if the execution has stopped due to an error.
     *
     * @param error the error
     */
    protected void onError(Throwable error) {

    }

    /**
     * The priority of this task
     *
     * @return the priority
     */
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

    /**
     * State of a task representing its lifecycle
     */
    public enum State {

        /**
         * Indicates that this task has not been submitted yet
         */
        NONE,
        /**
         * The task has been submitted to the task queue, and is waiting to be executed
         */
        ENQUEUED,
        /**
         * The task has been canceled
         */
        CANCELED,
        /**
         * The task is being executed right now
         */
        RUNNING,
        /**
         * The task has stopped due to an unexpected error
         */
        FAILURE,
        /**
         * The task has successfully finished execution
         */
        SUCCESS

    }
}
