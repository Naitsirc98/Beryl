package naitsirc98.beryl.graphics.rendering;

import naitsirc98.beryl.util.Destructor;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;

@Destructor
public interface Renderer extends NativeResource {

    void begin(MemoryStack stack);

    void end(MemoryStack stack);

}
