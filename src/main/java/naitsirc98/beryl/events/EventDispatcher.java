package naitsirc98.beryl.events;

import naitsirc98.beryl.concurrency.FixedWorkerPool;
import naitsirc98.beryl.concurrency.WorkerPool;

import java.util.List;
import java.util.Map;

final class EventDispatcher {

    private final WorkerPool workerPool;
    private final Map<Class<? extends Event>, List<EventCallback<?>>> eventCallbacks;

    EventDispatcher(Map<Class<? extends Event>, List<EventCallback<?>>> eventCallbacks) {
        this.workerPool = new FixedWorkerPool("EventDispatcherWorkerPool", 10);
        this.eventCallbacks = eventCallbacks;
    }

    void shutdown() {
        workerPool.terminate();
    }

    void await() {
        workerPool.await();
    }

    void dispatch(Event event) {
        processEvent(event);
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

        if(eventCallbacks == null) {
            return;
        }

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
