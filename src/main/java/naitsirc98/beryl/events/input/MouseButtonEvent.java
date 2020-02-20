package naitsirc98.beryl.events.input;

import naitsirc98.beryl.events.Event;
import naitsirc98.beryl.input.MouseButton;

public abstract class MouseButtonEvent extends Event {
	
	private final MouseButton button;

	public MouseButtonEvent(MouseButton button) {
		this.button = button;
	}

	public MouseButton button() {
		return button;
	}

	@Override
	public Class<? extends Event> type() {
		return MouseButtonEvent.class;
	}
}
