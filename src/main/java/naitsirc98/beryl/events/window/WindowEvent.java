package naitsirc98.beryl.events.window;

import naitsirc98.beryl.events.Event;

public abstract class WindowEvent extends Event {

	@Override
	public Class<? extends Event> type() {
		return WindowEvent.class;
	}
}




