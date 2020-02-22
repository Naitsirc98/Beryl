package naitsirc98.beryl.events.input.keyboard;

import naitsirc98.beryl.input.Key;
import naitsirc98.beryl.input.KeyModifier;

import java.util.Set;

public class KeyPressedEvent extends KeyEvent {

	public KeyPressedEvent(Key key, Set<KeyModifier> modifiers) {
		super(key, modifiers);
	}

}
