package naitsirc98.beryl.events.input.keyboard;

import naitsirc98.beryl.input.Key;
import naitsirc98.beryl.input.Modifier;

import java.util.Set;

public class KeyPressedEvent extends KeyEvent {

	public KeyPressedEvent(Key key, Set<Modifier> modifiers) {
		super(key, modifiers);
	}

}
