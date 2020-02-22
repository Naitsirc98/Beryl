package naitsirc98.beryl.events.input.mouse;

import naitsirc98.beryl.input.KeyModifier;
import naitsirc98.beryl.input.MouseButton;

import java.util.Set;

public class MouseButtonReleasedEvent extends MouseButtonEvent {

	public MouseButtonReleasedEvent(MouseButton button, Set<KeyModifier> modifiers) {
		super(button, modifiers);
	}



}
