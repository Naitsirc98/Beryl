package naitsirc98.beryl.events.input.mouse;

public class MouseScrollEvent extends MouseEvent {
	
	private final float xOffset;
	private final float yOffset;
	
	public MouseScrollEvent(float xOffset, float yOffset) {
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}
	
	public float getXOffset() {
		return xOffset;
	}
	
	public float getYOffset() {
		return yOffset;
	}

}