package naitsirc98.beryl.graphics.opengl.rendering;

import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.logging.Logger;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.opengl.GL45.glVertexArrayAttribFormat;

public class Init {

    private static final float[] VERTICES = {
            -0.5f, -0.5f, 0.0f, // left
            0.5f, -0.5f, 0.0f, // right
            0.0f,  0.5f, 0.0f  // top
    };

    // Vertex shader
    private static final String VERT_SRC = "#version 330 core\n"
            + "layout(location = 0) in vec3 aPos;\n"
            + "void main()\n"
            + "{\n"
            + "    gl_Position = vec4(aPos, 1.0f);\n"
            + "}";

    // Fragment shader
    private static final String FRAG_SRC = "#version 330 core\n"
            + "out vec4 FragColor;\n"
            + "void main()\n"
            + "{\n"
            + "    FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);\n"
            + "}";

    public static int vao, vbo;
    public static int shader;

    public static void init() {

        // Create shaders
        final int vertex = createShader(GL_VERTEX_SHADER, VERT_SRC);
        final int fragment = createShader(GL_FRAGMENT_SHADER, FRAG_SRC);
        // Build and compile the shader program
        shader = createShaderProgram(vertex, fragment);
        // Delete shaders
        glDeleteShader(vertex);
        glDeleteShader(fragment);

        vao = glCreateVertexArrays();
        vbo = glCreateBuffers();
        setUpVertexData(vao, vbo);

        System.out.println(glGetString(GL_VERSION));
    }

    private static void setUpVertexData(int vao, int vbo) {

        glNamedBufferStorage(vbo, VERTICES, 0);

        glVertexArrayVertexBuffer(vao, 0, vbo, 0, 12);

        glVertexArrayAttribBinding(vao, 0, 0);
        glEnableVertexArrayAttrib(vao, 0);
        glVertexArrayAttribFormat(vao, 0, 3, GL_FLOAT, false, 0);
    }

    private static int createShaderProgram(int vertexShader, int fragmentShader) {

        final int program = glCreateProgram();

        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);

        glLinkProgram(program);

        // Check for linking errors

        try(MemoryStack stack = MemoryStack.stackPush()) {

            IntBuffer success = stack.mallocInt(1);

            glGetProgramiv(program, GL_LINK_STATUS, success);

            if(success.get(0) == GL_FALSE) {

                final String infoLog = glGetProgramInfoLog(program);

                Logger.getAnonymousLogger().severe("Shader Linking Error: " + infoLog);
            }

        }

        return program;
    }

    private static int createShader(int shaderType, String src) {

        final int shader = glCreateShader(shaderType);

        glShaderSource(shader, src);

        glCompileShader(shader);

        // Since in Java is not possible to pass pointers to primitive data,
        // we need to simulate it by using LWJGL's MemoryStack class
        // It will allow us to create 'stack allocated' variables wrapped in Java NIO
        // Buffer Objects
        // Try with resources will clear all the stack allocated variables for us
        try(MemoryStack stack = MemoryStack.stackPush()) {

            IntBuffer success = stack.mallocInt(1);

            glGetShaderiv(shader, GL_COMPILE_STATUS, success);

            if(success.get(0) == GL_FALSE) {

                final String infoLog = glGetShaderInfoLog(shader);

                Logger.getAnonymousLogger().severe("Shader Compilation failed: " + infoLog);

            }

        }

        return shader;
    }

}
