package naitsirc98.beryl.events.input;

import naitsirc98.beryl.events.Event;
import naitsirc98.beryl.input.Key;
import naitsirc98.beryl.input.State;

public abstract class KeyEvent extends Event {
	
	private final Key key;
	
	public KeyEvent(Key key) {
		this.key = key;
	}
	
	public Key key() {
		return key;
	}

	@Override
	public Class<? extends Event> type() {
		return KeyEvent.class;
	}
}