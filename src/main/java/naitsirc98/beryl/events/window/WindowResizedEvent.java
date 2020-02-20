package naitsirc98.beryl.events.window;

public class WindowResizedEvent extends WindowEvent {
	
	private final int width;
	private final int height;

	public WindowResizedEvent(int w, int h) {
		this.width = w;
		this.height = h;
	}

	public int width() {
		return width;
	}

	public int height() {
		return height;
	}
	
}