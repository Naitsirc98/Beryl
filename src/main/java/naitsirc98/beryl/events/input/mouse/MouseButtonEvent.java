package naitsirc98.beryl.events.input.mouse;

import naitsirc98.beryl.events.Event;
import naitsirc98.beryl.input.KeyModifier;
import naitsirc98.beryl.input.MouseButton;

import java.util.Set;

public abstract class MouseButtonEvent extends Event {
	
	private final MouseButton button;
	private final Set<KeyModifier> modifiers;

	public MouseButtonEvent(MouseButton button, Set<KeyModifier> modifiers) {
		this.button = button;
		this.modifiers = modifiers;
	}

	public MouseButton button() {
		return button;
	}

	public Set<KeyModifier> modifiers() {
		return modifiers;
	}

	@Override
	public Class<? extends Event> type() {
		return MouseButtonEvent.class;
	}
}
