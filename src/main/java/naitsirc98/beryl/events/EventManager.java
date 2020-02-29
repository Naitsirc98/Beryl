package naitsirc98.beryl.events;

import naitsirc98.beryl.core.Beryl;
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

    public static final boolean DEBUG_REPORT_ENABLED = BerylConfiguration.EVENTS_DEBUG_REPORT.get(Beryl.DEBUG);

    @Singleton
    private static EventManager instance;

    /**
     * Adds an event callback for a particular event class at the back of its list
     *
     * @param eventClass the event class
     * @param callback   the callback
     */
    public static <T extends Event> void addEventCallback(Class<T> eventClass, EventCallback<T> callback) {
        instance.addEventCallbackInternal(assertNonNull(eventClass), assertNonNull(callback));
    }

    /**
     *  Adds an event callback for a particular event class at the front of its list
     *
     * @param eventClass the event class
     * @param callback   the callback
     */
    public static <T extends Event> void pushEventCallback(Class<T> eventClass, EventCallback<T> callback) {
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
     * Submit a new event and process it immediately
     *
     * @param event the event
     */
    public static void triggerEventNow(Event event) {
        instance.dispatcher.dispatch(event);
    }

    /**
     * Submit a new event to be processed in the next frame
     *
     * @param event the event
     */
    public static void triggerEvent(Event event) {
        instance.frontEventQueue.add(assertNonNull(event));
    }

    /**
     * Submit a new event to be processed after the next frame
     *
     * @param event the event
     */
    public static void triggerLater(Event event) {
        instance.backEventQueue.add(assertNonNull(event));
    }

    private Queue<Event> frontEventQueue;
    private Queue<Event> backEventQueue;
    private Map<Class<? extends Event>, List<EventCallback<?>>> eventCallbacks;
    private EventDispatcher dispatcher;
    private EventDebugReport debugReport;

    private EventManager() {
    }

    @Override
    protected void init() {
        this.frontEventQueue = new ArrayDeque<>(BerylConfiguration.EVENT_QUEUE_INITIAL_CAPACITY.get(128));
        this.backEventQueue = new ArrayDeque<>(BerylConfiguration.EVENT_QUEUE_INITIAL_CAPACITY.get(128));
        this.eventCallbacks = new HashMap<>();
        dispatcher = new EventDispatcher(eventCallbacks);
        debugReport = DEBUG_REPORT_ENABLED ? new EventDebugReport() : null;
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

        if(DEBUG_REPORT_ENABLED) {
            debugReport.count(frontEventQueue.size());
        }

        processEventQueue();

        swapEventQueues();
    }

    /**
     * Returns the debug report of the event manager
     *
     * @return the debug report, or null if debug reports are disabled
     * */
    public CharSequence debugReport() {
        return DEBUG_REPORT_ENABLED ? instance.debugReport.report() : null;
    }

    private void processEventQueue() {

        final Queue<Event> eventQueue = frontEventQueue;
        final EventDispatcher dispatcher = this.dispatcher;

        if(eventQueue.isEmpty()) {
            return;
        }

        while(!eventQueue.isEmpty()) {
            final Event event = eventQueue.poll();
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

    private class EventDebugReport {

        private int eventCount;
        private int maxEventCount;

        private void count(int count) {
            eventCount += count;
        }

        public String report() {

            maxEventCount = Math.max(maxEventCount, eventCount);
            final int eventCount = this.eventCount;
            this.eventCount = 0;

            return "Event count: " + eventCount + " | Max Event count: " + maxEventCount;
        }
    }
}
