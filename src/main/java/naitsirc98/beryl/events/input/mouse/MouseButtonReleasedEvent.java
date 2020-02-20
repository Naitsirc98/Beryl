package naitsirc98.beryl.events.input.mouse;

import naitsirc98.beryl.input.Modifier;
import naitsirc98.beryl.input.MouseButton;

import java.util.Set;

public class MouseButtonReleasedEvent extends MouseButtonEvent {

	public MouseButtonReleasedEvent(MouseButton button, Set<Modifier> modifiers) {
		super(button, modifiers);
	}



}
