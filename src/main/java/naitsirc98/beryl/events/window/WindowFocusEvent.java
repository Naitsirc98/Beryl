package naitsirc98.beryl.events.window;

public class WindowFocusEvent extends WindowEvent {

	private final boolean focused;
	
	public WindowFocusEvent(boolean focused) {
		this.focused = focused;
	}

	public boolean focused() {
		return focused;
	}
	
}