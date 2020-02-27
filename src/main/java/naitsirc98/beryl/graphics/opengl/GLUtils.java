package naitsirc98.beryl.graphics.opengl;

import naitsirc98.beryl.util.DataType;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DOUBLE;

public class GLUtils {

    public static int toGL(DataType dataType) {
        switch(dataType) {
            case INT8:
                return GL_BYTE;
            case UINT8:
                return GL_UNSIGNED_BYTE;
            case INT16:
                return GL_SHORT;
            case UINT16:
                return GL_UNSIGNED_SHORT;
            case INT32:
                return GL_INT;
            case UINT32:
                return GL_UNSIGNED_INT;
            case FLOAT16:
            case FLOAT24:
            case FLOAT32:
                return GL_FLOAT;
            case DOUBLE:
                return GL_DOUBLE;
            default:
                throw new IllegalArgumentException("Unsupported data type: " + dataType);
        }
    }

}
