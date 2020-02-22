package naitsirc98.beryl.events.input.keyboard;

import naitsirc98.beryl.input.Key;
import naitsirc98.beryl.input.KeyModifier;

import java.util.Set;

public class KeyReleasedEvent extends KeyEvent {

	public KeyReleasedEvent(Key key, Set<KeyModifier> modifiers) {
		super(key, modifiers);
	}
	
}