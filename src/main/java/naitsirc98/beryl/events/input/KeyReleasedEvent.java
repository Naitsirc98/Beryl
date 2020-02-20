package naitsirc98.beryl.events.input;

import naitsirc98.beryl.input.Key;
import naitsirc98.beryl.input.State;
import org.lwjgl.glfw.GLFW;

public class KeyReleasedEvent extends KeyEvent {

	public KeyReleasedEvent(Key key) {
		super(key);
	}
	
}