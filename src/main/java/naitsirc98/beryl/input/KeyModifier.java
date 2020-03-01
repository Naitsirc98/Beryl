package naitsirc98.beryl.input;

import naitsirc98.beryl.util.types.EnumMapper;
import naitsirc98.beryl.util.GLFWWrapper;

import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public enum KeyModifier implements GLFWWrapper {

    MOD_SHIFT(GLFW_MOD_SHIFT),
    MOD_CONTROL(GLFW_MOD_CONTROL),
    MOD_ALT(GLFW_MOD_ALT),
    MOD_SUPER(GLFW_MOD_SUPER),
    MOD_NUM_LOCK(GLFW_MOD_NUM_LOCK);

    private static final EnumMapper<KeyModifier, Integer> MAPPER;
    static {
        MAPPER = EnumMapper.of(KeyModifier.class, GLFWWrapper::glfwHandle);
    }

    private static final Map<Integer, Set<KeyModifier>> MASKS_CACHE = new WeakHashMap<>();

    public static Set<KeyModifier> asModifierMask(int flags) {

        if(MASKS_CACHE.containsKey(flags)) {
            return MASKS_CACHE.get(flags);
        }

        EnumSet<KeyModifier> mask = EnumSet.noneOf(KeyModifier.class);

        if((flags & GLFW_MOD_SHIFT) == GLFW_MOD_SHIFT) {
            mask.add(MOD_SHIFT);
        } else if((flags & GLFW_MOD_CONTROL) == GLFW_MOD_CONTROL) {
            mask.add(MOD_CONTROL);
        } else if((flags & GLFW_MOD_ALT) == GLFW_MOD_ALT) {
            mask.add(MOD_ALT);
        } else if((flags & GLFW_MOD_SUPER) == GLFW_MOD_SUPER) {
            mask.add(MOD_SUPER);
        } else if((flags & GLFW_MOD_NUM_LOCK) == GLFW_MOD_NUM_LOCK) {
            mask.add(MOD_NUM_LOCK);
        }

        MASKS_CACHE.put(flags, Collections.unmodifiableSet(mask));

        return mask;
    }

    public static KeyModifier asModifier(int glfwHandle) {
        return MAPPER.keyOf(glfwHandle);
    }

    private final int glfwHandle;

    KeyModifier(int glfwHandle) {
        this.glfwHandle = glfwHandle;
    }

    @Override
    public int glfwHandle() {
        return 0;
    }
}
