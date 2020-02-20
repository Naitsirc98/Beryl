package naitsirc98.beryl.events.input.mouse;

import naitsirc98.beryl.events.Event;

public class UnicodeInputEvent extends Event {

    private final int codePoint;

    public UnicodeInputEvent(int codePoint) {
        this.codePoint = codePoint;
    }

    public int codePoint() {
        return codePoint;
    }

    @Override
    public Class<? extends Event> type() {
        return UnicodeInputEvent.class;
    }
}
