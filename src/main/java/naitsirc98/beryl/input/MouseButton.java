package naitsirc98.beryl.input;

import naitsirc98.beryl.util.types.EnumMapper;
import naitsirc98.beryl.util.GLFWWrapper;

import static org.lwjgl.glfw.GLFW.*;

public enum MouseButton implements GLFWWrapper {

	MOUSE_BUTTON_1(GLFW_MOUSE_BUTTON_1),
	MOUSE_BUTTON_2(GLFW_MOUSE_BUTTON_2),
	MOUSE_BUTTON_3(GLFW_MOUSE_BUTTON_3),
	MOUSE_BUTTON_4(GLFW_MOUSE_BUTTON_4),
	MOUSE_BUTTON_5(GLFW_MOUSE_BUTTON_5),
	MOUSE_BUTTON_6(GLFW_MOUSE_BUTTON_6),
	MOUSE_BUTTON_7(GLFW_MOUSE_BUTTON_7),
	MOUSE_BUTTON_8(GLFW_MOUSE_BUTTON_8),
	MOUSE_BUTTON_LAST(GLFW_MOUSE_BUTTON_LAST),
	MOUSE_BUTTON_LEFT(GLFW_MOUSE_BUTTON_LEFT),
	MOUSE_BUTTON_RIGHT(GLFW_MOUSE_BUTTON_RIGHT),
	MOUSE_BUTTON_MIDDLE(GLFW_MOUSE_BUTTON_MIDDLE);

	private static final EnumMapper<MouseButton, Integer> MAPPER;
	static {
		MAPPER = EnumMapper.of(MouseButton.class, GLFWWrapper::glfwHandle);
	}

	public static MouseButton asMouseButton(int id) {
		return MAPPER.keyOf(id);
	}

	private final int glfwHandle;

	MouseButton(int glfwHandle) {
		this.glfwHandle = glfwHandle;
	}

	@Override
	public int glfwHandle() {
		return glfwHandle;
	}

}
