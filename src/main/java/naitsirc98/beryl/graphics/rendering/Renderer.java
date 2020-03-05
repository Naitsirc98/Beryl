package naitsirc98.beryl.graphics.rendering;

import naitsirc98.beryl.util.types.Destructor;
import org.lwjgl.system.NativeResource;

@Destructor
public interface Renderer extends NativeResource {

    void begin();

    void end();

}
