package naitsirc98.beryl.events.input;

import naitsirc98.beryl.input.Key;

public class KeyRepeatEvent extends KeyEvent {
	
	private final int repeatCount;

	public KeyRepeatEvent(Key key, int repeatCount) {
		super(key);
		this.repeatCount = repeatCount;
	}

	public int repeatCount() {
		return repeatCount;
	}
	
}
