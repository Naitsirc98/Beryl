package naitsirc98.beryl.graphics.shaders;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.GraphicsAPI;
import naitsirc98.beryl.graphics.ShaderStage;
import naitsirc98.beryl.logging.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.exists;
import static naitsirc98.beryl.core.BerylConfigConstants.PRINT_SHADERS_SOURCE;

public final class GLSLPreprocessor {

    private static final String DIRECTIVE_CHARACTER = "@";
    private static final Path SHADERS_ROOT = BerylFiles.getPath("shaders");
    private static final String BERYL_GLSL_METADATA = createBerylGLSLMetadata();

    private final StringBuffer sourceBuffer;
    private final Path path;
    private final ShaderStage stage;

    public GLSLPreprocessor(Path path, ShaderStage stage) {
        this.path = path;
        this.stage = stage;
        sourceBuffer = new StringBuffer();
    }

    public String process() {

        if(sourceBuffer.length() > 0) {
            return sourceBuffer.toString();
        }

        try {
            processShaderLines();
        } catch (IOException e) {
            Log.error("Could not process shader file: " + path, e);
        }

        final String source = sourceBuffer.toString();

        if(PRINT_SHADERS_SOURCE) {
            printShaderSource(source);
        }

        return source;
    }

    private void printShaderSource(String source) {

        StringBuilder stringBuilder = new StringBuilder(String.format("%s shader '%s' source:\n", stage, path));

        String[] lines = source.split("\n");

        for(int i = 0;i < lines.length;i++) {
            stringBuilder.append(i)
                    .append(": ")
                    .append(lines[i])
                    .append('\n');
        }

        Log.trace(stringBuilder);
    }

    private void processShaderLines() throws IOException {
        Files.lines(path)
                .parallel()
                .map(this::processDirective)
                .forEachOrdered(this::appendLine);
    }

    private void appendLine(String line) {
        sourceBuffer.append(line).append('\n');
    }

    private String processDirective(String line) {

        if(!line.startsWith(DIRECTIVE_CHARACTER)) {
            return line;
        }

        int directiveEnd = line.indexOf(' ');

        if(directiveEnd < 0) {
            directiveEnd = line.length();
        }

        return getDirective(line.substring(1, directiveEnd)).process(line.substring(directiveEnd).trim());
    }

    private Directive getDirective(String name) {
        switch(name) {
            case "beryl":
                return new BerylMetadataDirective();
            case "include":
                return new IncludeDirective();
        }
        throw new IllegalArgumentException("Unknown GLSL directive name: " + name);
    }


    private interface Directive {
        String process(String arg);
    }

    private class BerylMetadataDirective implements Directive {

        @Override
        public String process(String arg) {
            return BERYL_GLSL_METADATA + define(stage.name()) + '\n';
        }
    }

    private class IncludeDirective implements Directive {

        @Override
        public String process(String file) {

            file = file.replaceAll("\"", "");

            Path includePath;

            if(!exists(includePath = path.getParent().resolve(file)) && !exists(includePath = SHADERS_ROOT.resolve(file))) {
                throw new RuntimeException("Trying to include file " + includePath + " to GLSL Shader, but it does not exists");
            }

            return new GLSLPreprocessor(includePath, stage).process();
        }
    }

    private static String createBerylGLSLMetadata() {

        StringBuilder builder = new StringBuilder();

        builder.append("#version ").append(GraphicsAPI.get().minGLSLVersion()).append('\n');
        builder.append("#ifndef ").append(GraphicsAPI.get().name()).append('\n');
        builder.append(define(GraphicsAPI.get().name()));
        builder.append("#endif\n");
        builder.append("#extension GL_KHR_vulkan_glsl: enable\n");

        return builder.toString();
    }

    private static String define(Object name, Object value) {
        return "#define " + name + " " + value + "\n";
    }

    private static String define(Object name) {
        return "#define " + name + "\n";
    }
}
