package naitsirc98.beryl.events.window;

public class WindowMovedEvent extends WindowEvent {
	
	private final int x;
	private final int y;

	public WindowMovedEvent(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int x() {
		return x;
	}

	public int y() {
		return y;
	}
	
	
}
