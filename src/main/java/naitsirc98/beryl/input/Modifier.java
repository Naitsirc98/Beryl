package naitsirc98.beryl.input;

import naitsirc98.beryl.util.EnumMapper;
import naitsirc98.beryl.util.GLFWWrapper;

import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public enum Modifier implements GLFWWrapper {

    MOD_SHIFT(GLFW_MOD_SHIFT),
    MOD_CONTROL(GLFW_MOD_CONTROL),
    MOD_ALT(GLFW_MOD_ALT),
    MOD_SUPER(GLFW_MOD_SUPER),
    MOD_NUM_LOCK(GLFW_MOD_NUM_LOCK);

    private static final EnumMapper<Modifier, Integer> MAPPER;
    static {
        MAPPER = EnumMapper.of(Modifier.class, GLFWWrapper::glfwHandle);
    }

    private static final Map<Integer, Set<Modifier>> MASKS_CACHE = new WeakHashMap<>();

    public static Set<Modifier> asModifierMask(int flags) {

        if(MASKS_CACHE.containsKey(flags)) {
            return MASKS_CACHE.get(flags);
        }

        EnumSet<Modifier> mask = EnumSet.noneOf(Modifier.class);

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

    public static Modifier asModifier(int glfwHandle) {
        return MAPPER.keyOf(glfwHandle);
    }

    private final int glfwHandle;

    Modifier(int glfwHandle) {
        this.glfwHandle = glfwHandle;
    }

    @Override
    public int glfwHandle() {
        return 0;
    }
}
