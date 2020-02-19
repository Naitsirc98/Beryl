package naitsirc98.beryl.events;

public abstract class Event {

    private boolean consumed;

    public boolean consumed() {
        return consumed;
    }

    public void consume() {
        consumed = true;
    }

    public abstract Class<? extends Event> type();

}
