package naitsirc98.beryl.util.types;

import static org.lwjgl.system.MemoryStack.stackPop;
import static org.lwjgl.system.MemoryStack.stackPush;

public abstract class StackBuilder<T> implements IBuilder<T> {

    protected StackBuilder() {
        stackPush();
    }

    public T buildAndPop() {
        final T result = build();
        pop();
        return result;
    }

    public void pop() {
        stackPop();
    }

}
