package naitsirc98.beryl.events.input.keyboard;

import naitsirc98.beryl.events.Event;
import naitsirc98.beryl.input.Key;
import naitsirc98.beryl.input.Modifier;

import java.util.Set;

public abstract class KeyEvent extends Event {
	
	private final Key key;
	private final Set<Modifier> modifiers;
	
	public KeyEvent(Key key, Set<Modifier> modifiers) {
		this.key = key;
		this.modifiers = modifiers;
	}
	
	public Key key() {
		return key;
	}

	public Set<Modifier> modifiers() {
		return modifiers;
	}

	@Override
	public Class<? extends Event> type() {
		return KeyEvent.class;
	}
}