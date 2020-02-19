package naitsirc98.beryl.events;

import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.util.Singleton;

import java.util.*;

import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static org.lwjgl.glfw.GLFW.*;

/**
 * The event manager process all the events that occur during a frame
 */
public final class EventManager extends BerylSystem {

    @Singleton
    private static EventManager instance;


    /**
     * Adds an event callback for a particular event class at the back of its list
     *
     * @param eventClass the event class
     * @param callback   the callback
     */
    public static void addEventCallback(Class<? extends Event> eventClass, EventCallback<? extends Event> callback) {
        instance.addEventCallbackInternal(assertNonNull(eventClass), assertNonNull(callback));
    }

    /**
     *  Adds an event callback for a particular event class at the front of its list
     *
     * @param eventClass the event class
     * @param callback   the callback
     */
    public static void pushEventCallback(Class<? extends Event> eventClass, EventCallback<? extends Event> callback) {
        instance.pushEventCallbackInternal(assertNonNull(eventClass), assertNonNull(callback));
    }

    /**
     * Remove an event callback.
     *
     * @param eventClass    the event class
     * @param eventCallback the event callback
     */
    public static void removeEventCallback(Class<? extends Event> eventClass, EventCallback<?> eventCallback) {
        instance.removeEventCallbackInternal(assertNonNull(eventClass), eventCallback);
    }

    /**
     * Submit a new event to be processed in the next frame
     *
     * @param event the event
     */
    public static void submit(Event event) {
        instance.frontEventQueue.add(assertNonNull(event));
    }

    /**
     * Submit a new event to be processed after the next frame
     *
     * @param event the event
     */
    public static void submitLater(Event event) {
        instance.backEventQueue.add(assertNonNull(event));
    }

    private Queue<Event> frontEventQueue;
    private Queue<Event> backEventQueue;
    private Map<Class<? extends Event>, List<EventCallback<?>>> eventCallbacks;
    private EventDispatcher dispatcher;

    private EventManager() {

    }

    @Override
    protected void init() {
        this.frontEventQueue = new ArrayDeque<>(BerylConfiguration.EVENT_QUEUE_INITIAL_CAPACITY.get(64));
        this.backEventQueue = new ArrayDeque<>(BerylConfiguration.EVENT_QUEUE_INITIAL_CAPACITY.get(64));
        this.eventCallbacks = new HashMap<>();
        dispatcher = new EventDispatcher(eventCallbacks);
    }

    @Override
    protected void terminate() {
        dispatcher.shutdown();
    }

    /**
     * Wait for events.
     */
    public void waitForEvents() {
        glfwWaitEvents();
    }

    /**
     * Wait for events for a certain timeout of time
     *
     * @param timeout the timeout in seconds
     */
    public void waitForEvents(double timeout) {
        glfwWaitEventsTimeout(timeout);
    }

    /**
     * Process events.
     */
    public void processEvents() {

        glfwPollEvents();

        processEventQueue();

        swapEventQueues();
    }

    private void processEventQueue() {

        if(frontEventQueue.isEmpty()) {
            return;
        }

        while(!frontEventQueue.isEmpty()) {
            final Event event = frontEventQueue.poll();
            dispatcher.dispatch(event);
        }
    }

    private void swapEventQueues() {
        final Queue<Event> tmp = this.frontEventQueue;
        this.frontEventQueue = backEventQueue;
        backEventQueue = tmp;
    }

    private void addEventCallbackInternal(Class<? extends Event> eventClass, EventCallback<? extends Event> callback) {

        List<EventCallback<?>> callbacks = eventCallbacks.computeIfAbsent(eventClass, k -> new ArrayList<>(1));

        callbacks.add(callback);
    }

    private void pushEventCallbackInternal(Class<? extends Event> eventClass, EventCallback<? extends Event> callback) {

        List<EventCallback<?>> callbacks = eventCallbacks.computeIfAbsent(eventClass, k -> new ArrayList<>(1));

        callbacks.add(0, callback);
    }

    private void removeEventCallbackInternal(Class<? extends Event> eventClass, EventCallback<?> eventCallback) {
        List<EventCallback<?>> callbacks = eventCallbacks.get(eventClass);
        if(callbacks != null) {
            callbacks.remove(eventCallback);
        }
    }
}