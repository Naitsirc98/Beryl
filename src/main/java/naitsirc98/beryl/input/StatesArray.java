package naitsirc98.beryl.input;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static naitsirc98.beryl.input.State.*;

public class StatesArray<T extends Enum<T>> implements Iterable<Map.Entry<T, State>> {

    private final EnumMap<T, State> states;

    public StatesArray(Class<T> type) {
        states = new EnumMap<>(type);
    }

    public StatesArray<T> clear() {
        states.clear();
        return this;
    }

    public StatesArray<T> set(T key, State state) {
        states.put(key, state);
        return this;
    }

    public State stateOf(T key) {
        return states.getOrDefault(key, RELEASE);
    }

    public boolean isPressed(T key) {
        return PRESS == stateOf(key);
    }

    public boolean isReleased(T key) {
        return RELEASE == stateOf(key);
    }

    public boolean isRepeat(T key) {
        return REPEAT == stateOf(key);
    }

    public boolean isType(T key) {
        return TYPE == stateOf(key);
    }

    public boolean isClick(T key) {
        return CLICK == stateOf(key);
    }

    public Set<T> withState(State state) {
        return states.keySet().stream().filter(key -> stateOf(key).equals(state)).collect(toSet());
    }

    public Set<T> pressed() {
        return withState(PRESS);
    }

    public Set<T> released() {
        return withState(RELEASE);
    }

    public Set<T> repeated() {
        return withState(REPEAT);
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<T, State>> iterator() {
        return states.entrySet().iterator();
    }
}
