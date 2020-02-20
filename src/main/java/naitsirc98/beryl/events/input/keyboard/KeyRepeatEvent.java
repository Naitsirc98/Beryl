package naitsirc98.beryl.events.input.keyboard;

import naitsirc98.beryl.input.Key;
import naitsirc98.beryl.input.Modifier;

import java.util.Set;

public class KeyRepeatEvent extends KeyEvent {
	
	private final int repeatCount;

	public KeyRepeatEvent(Key key, Set<Modifier> modifiers, int repeatCount) {
		super(key, modifiers);
		this.repeatCount = repeatCount;
	}

	public int repeatCount() {
		return repeatCount;
	}
	
}
