package naitsirc98.beryl.events.input.keyboard;

import naitsirc98.beryl.input.Key;
import naitsirc98.beryl.input.Modifier;

import java.util.Set;

public class KeyTypedEvent extends KeyEvent {

	public KeyTypedEvent(Key key, Set<Modifier> modifiers) {
		super(key, modifiers);
	}

}
