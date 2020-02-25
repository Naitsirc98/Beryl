package naitsirc98.beryl.graphics.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkLayerProperties;

import java.nio.IntBuffer;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static naitsirc98.beryl.graphics.vulkan.VulkanContext.VALIDATION_LAYERS;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.vkEnumerateInstanceLayerProperties;

public class VulkanValidationLayers {

    public static boolean validationLayersSupported() {
        return availableValidationLayers().containsAll(VALIDATION_LAYERS);
    }

    public static Set<String> defaultValidationLayers() {
        return Stream.of("VK_LAYER_KHRONOS_validation").collect(toSet());
    }

    private static Set<String> availableValidationLayers() {

        try(MemoryStack stack = stackPush()) {

            IntBuffer layerCount = stack.ints(0);

            vkEnumerateInstanceLayerProperties(layerCount, null);

            VkLayerProperties.Buffer availableLayers = VkLayerProperties.mallocStack(layerCount.get(0), stack);

            vkEnumerateInstanceLayerProperties(layerCount, availableLayers);

            return availableLayers.stream()
                    .map(VkLayerProperties::layerNameString)
                    .collect(toSet());
        }
    }

}
