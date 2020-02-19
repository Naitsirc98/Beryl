package naitsirc98.beryl.events;

import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.core.Log;
import naitsirc98.beryl.util.Singleton;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static org.lwjgl.glfw.GLFW.*;

public final class EventManager extends BerylSystem {

    private static final long EVENT_PROCESSING_TIMEOUT = 360;


    @Singleton
    private static EventManager instance;

    public static void addEventCallback(Class<? extends Event> eventClass, EventCallback<? extends Event> callback) {
        instance.addEventCallbackInternal(assertNonNull(eventClass), assertNonNull(callback));
    }

    public static void pushEventCallback(Class<? extends Event> eventClass, EventCallback<? extends Event> callback) {
        instance.pushEventCallbackInternal(assertNonNull(eventClass), assertNonNull(callback));
    }

    public static void removeEventCallback(Class<? extends Event> eventClass, EventCallback<?> eventCallback) {
        instance.removeEventCallbackInternal(assertNonNull(eventClass), eventCallback);
    }

    public static void submit(Event event) {
        instance.eventQueue.add(assertNonNull(event));
    }

    public static void submitLater(Event event) {
        instance.eventQueueLater.add(assertNonNull(event));
    }

    private Queue<Event> eventQueue;
    private Queue<Event> eventQueueLater;
    private Map<Class<? extends Event>, List<EventCallback<?>>> eventCallbacks;
    private ExecutorService threadPool;

    private EventManager() {

    }

    @Override
    protected void init() {
        this.eventQueue = new ArrayDeque<>(BerylConfiguration.EVENT_QUEUE_INITIAL_CAPACITY.get(64));
        this.eventQueueLater = new ArrayDeque<>();
        this.eventCallbacks = new HashMap<>();
        threadPool = Executors.newCachedThreadPool();
    }

    public void waitForEvents() {
        glfwWaitEvents();
    }

    public void waitForEvents(double timeout) {
        glfwWaitEventsTimeout(timeout);
    }

    public void processEvents() {

        glfwPollEvents();

        processEventQueue();

        submitLaterEvents();
    }

    private void processEventQueue() {

        if(eventQueue.isEmpty()) {
            return;
        }

        while(!eventQueue.isEmpty()) {
            threadPool.submit(() -> processEvent(eventQueue.remove()));
        }

        try {
            threadPool.awaitTermination(EVENT_PROCESSING_TIMEOUT, MILLISECONDS);
        } catch (InterruptedException e) {
            Log.error("Timeout at waiting for event processing", e);
        }
    }

    private void submitLaterEvents() {
        while(!eventQueueLater.isEmpty()) {
            eventQueue.add(eventQueueLater.remove());
        }
    }

    private void processEvent(Event event) {

        if(eventCallbacks.containsKey(event.getClass())) {
            processEvent(event, eventCallbacks.get(event.getClass()));
        } else {
            processEvent(event, eventCallbacks.get(event.type()));
        }
    }

    private void processEvent(Event event, List<EventCallback<?>> eventCallbacks) {

        for(EventCallback<?> callback : eventCallbacks) {

            call(event, callback);

            if(event.consumed()) {
                break;
            }
        }

    }

    @SuppressWarnings("unchecked")
    private <T extends Event> void call(T event, EventCallback<?> callback) {
        ((EventCallback<T>)callback).onEvent(event);
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
