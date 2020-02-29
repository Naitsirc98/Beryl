package naitsirc98.beryl.events;

import naitsirc98.beryl.logging.Log;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

final class EventDispatcher {

    private final ExecutorService threadPool;
    private final Map<Class<? extends Event>, List<EventCallback<?>>> eventCallbacks;

    EventDispatcher(Map<Class<? extends Event>, List<EventCallback<?>>> eventCallbacks) {
        this.threadPool = Executors.newCachedThreadPool();
        this.eventCallbacks = eventCallbacks;
    }

    void shutdown() {
        threadPool.shutdown();
        await();
    }

    void await() {
        try {
            threadPool.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.error("Timeout error while waiting for events", e);
        }
    }

    void dispatch(Event event) {
        threadPool.submit(() -> processEvent(event));
    }

    private void processEvent(Event event) {

        if(eventCallbacks.containsKey(event.getClass())) {
            processEvent(event, eventCallbacks.get(event.getClass()));
        }

        if(!event.consumed()) {
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

}
