package naitsirc98.beryl.graphics.shaders;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.resources.Resources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GLSLPreprocessor {

    private static final String DIRECTIVE_CHARACTER = "@";
    private static final Path SHADERS_ROOT = Resources.getPath("shaders");

    private final StringBuffer sourceBuffer;
    private final Path path;

    public GLSLPreprocessor(Path path) {
        this.path = path;
        sourceBuffer = new StringBuffer();
    }

    public void reset() {
        sourceBuffer.delete(0, sourceBuffer.length());
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
        return sourceBuffer.toString();
    }

    private void processShaderLines() throws IOException {
        Files.lines(path)
                .parallel()
                .map(String::trim)
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

        final int directiveEnd = line.indexOf(' ');

        if(directiveEnd < 0) {
            throw new RuntimeException("Bad GLSL Directive syntax: " + line);
        }

        return getDirective(line.substring(1, directiveEnd)).process(line.substring(directiveEnd).trim());
    }

    private Directive getDirective(String name) {
        switch(name) {
            case "include":
                return new IncludeDirective();
        }
        throw new IllegalArgumentException("Unknown GLSL directive name: " + name);
    }


    private interface Directive {
        String process(String arg);
    }

    private static class IncludeDirective implements Directive {

        @Override
        public String process(String file) {

            Path path = SHADERS_ROOT.resolve(file.replaceAll("\"", ""));

            if(!Files.exists(path)) {
                throw new RuntimeException("Trying to include file " + path + " to GLSL Shader, but it does not exists");
            }

            return new GLSLPreprocessor(path).process();
        }
    }
}
