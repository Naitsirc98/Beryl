package naitsirc98.beryl.events.input;

import naitsirc98.beryl.events.Event;

public abstract class MouseEvent extends Event {

    @Override
    public Class<? extends Event> type() {
        return MouseEvent.class;
    }
}
