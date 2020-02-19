package naitsirc98.beryl.events;

public interface EventCallback<T extends Event> {

    void onEvent(T event);

}
