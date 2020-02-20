package naitsirc98.beryl.events.input;

public class MouseMovedEvent extends MouseEvent {
	
	private final float x;
	private final float y;
	
	public MouseMovedEvent(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public float x() {
		return x;
	}
	
	public float y() {
		return y;
	}
	
	@Override 
	public String toString() {
		return "Mouse Moved: (" + x + ", " + y + ")";
	}
	
}

