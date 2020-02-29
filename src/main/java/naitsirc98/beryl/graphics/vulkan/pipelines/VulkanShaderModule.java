package naitsirc98.beryl.graphics.vulkan.pipelines;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.ShaderStage;
import naitsirc98.beryl.graphics.shaders.SPIRVBytecode;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import java.nio.LongBuffer;
import java.nio.file.Path;

import static naitsirc98.beryl.graphics.shaders.SPIRVCompiler.compileShaderFile;
import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanShaderModule implements NativeResource {

    public static final String DEFAULT_ENTRY_POINT = "main";

    private long vkShaderModule;
    private final ShaderStage stage;

    public VulkanShaderModule(Path shaderFile, ShaderStage stage) {
        this.stage = stage;
        vkShaderModule = createShaderModule(compileShaderFile(shaderFile, stage));
    }

    public long vkShaderModule() {
        return vkShaderModule;
    }

    public ShaderStage stage() {
        return stage;
    }

    @Override
    public void free() {
        vkDestroyShaderModule(Graphics.vulkan().vkLogicalDevice(), vkShaderModule, null);
        vkShaderModule = VK_NULL_HANDLE;
    }


    private long createShaderModule(SPIRVBytecode spirvShader) {

        try(MemoryStack stack = stackPush()) {

            VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.callocStack(stack);

            createInfo.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
            createInfo.pCode(spirvShader.bytecode());

            LongBuffer pShaderModule = stack.mallocLong(1);

            vkCall(vkCreateShaderModule(Graphics.vulkan().vkLogicalDevice(), createInfo, null, pShaderModule));

            spirvShader.free();

            return pShaderModule.get(0);
        }
    }
}