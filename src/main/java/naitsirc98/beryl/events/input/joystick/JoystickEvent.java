package naitsirc98.beryl.events.input.joystick;

import naitsirc98.beryl.events.Event;
import naitsirc98.beryl.input.Joystick;

public abstract class JoystickEvent extends Event {

    private final Joystick joystick;

    protected JoystickEvent(Joystick joystick) {
        this.joystick = joystick;
    }

    public Joystick joystick() {
        return joystick;
    }

    @Override
    public Class<? extends Event> type() {
        return JoystickEvent.class;
    }
}
