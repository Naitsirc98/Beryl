package naitsirc98.beryl.events.input;

import naitsirc98.beryl.input.Modifier;
import naitsirc98.beryl.input.MouseButton;

import java.util.Set;

public class MouseButtonPressedEvent extends MouseButtonEvent {

	public MouseButtonPressedEvent(MouseButton button, Set<Modifier> modifiers) {
		super(button, modifiers);
	}

	

}
