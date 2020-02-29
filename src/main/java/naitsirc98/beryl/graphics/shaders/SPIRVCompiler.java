package naitsirc98.beryl.graphics.shaders;

import naitsirc98.beryl.graphics.ShaderStage;
import naitsirc98.beryl.logging.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.util.shaderc.Shaderc.*;

public final class SPIRVCompiler {

    private static final String ENTRY_POINT = "main";

    public static SPIRVBytecode compileShaderFile(Path path, ShaderStage stage) {
        try {
            String source = new String(Files.readAllBytes(path));
            return compileShader(path.toString(), source, stage);
        } catch (IOException e) {
            Log.error("Failed to compile file " + path + " to SPIRV");
        }
        return null;
    }

    public static SPIRVBytecode compileShader(String filename, String source, ShaderStage stage) {

        long compiler = shaderc_compiler_initialize();

        if(compiler == NULL) {
            Log.fatal("Failed to create shader compiler");
            return null;
        }

        long result = shaderc_compile_into_spv(compiler, source, getShaderKind(stage), filename, ENTRY_POINT, NULL);

        if(result == NULL) {
            Log.fatal("Failed to compile shader " + filename + " into SPIR-V");
        }

        if(shaderc_result_get_compilation_status(result) != shaderc_compilation_status_success) {
            Log.fatal("Failed to compile shader " + filename + "into SPIR-V:\n "
                    + shaderc_result_get_error_message(result));
        }

        shaderc_compiler_release(compiler);

        return new SPIRVBytecode(result, shaderc_result_get_bytes(result));
    }

    private static int getShaderKind(ShaderStage stage) {
        switch(stage) {
            case VERTEX_STAGE:
                return shaderc_vertex_shader;
            case TESSELATION_CONTROL_STAGE:
                return shaderc_tess_control_shader;
            case TESSELATION_EVALUATION_STAGE:
                return shaderc_tess_evaluation_shader;
            case GEOMETRY_STAGE:
                return shaderc_geometry_shader;
            case FRAGMENT_STAGE:
                return shaderc_fragment_shader;
        }
        return -1;
    }

}
