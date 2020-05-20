package naitsirc98.beryl.graphics.opengl.shaders;

public class UniformUtils {

    public static String uniformArrayName(String uniformName, int index) {
        return uniformName + "[" + index + "]";
    }

    private UniformUtils() {}
}
