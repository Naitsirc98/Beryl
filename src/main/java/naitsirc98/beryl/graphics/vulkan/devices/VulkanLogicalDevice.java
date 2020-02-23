package naitsirc98.beryl.graphics.vulkan.devices;

import naitsirc98.beryl.graphics.vulkan.devices.VulkanPhysicalDevice.QueueFamilyIndices;
import naitsirc98.beryl.util.Destructor;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.*;

import static naitsirc98.beryl.graphics.vulkan.VulkanContext.*;
import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.stringPointers;
import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

@Destructor
public class VulkanLogicalDevice implements NativeResource {

    public static VulkanLogicalDevice newVulkanLogicalDevice(VulkanPhysicalDevice physicalDevice) {
        return new VulkanLogicalDevice(physicalDevice);
    }

    private final VkDevice vkDevice;
    private final VkQueue graphicsQueue;
    private final VkQueue presentationQueue;

    private VulkanLogicalDevice(VulkanPhysicalDevice physicalDevice) {
        vkDevice = createVkDevice(physicalDevice);
        graphicsQueue = createVkQueue(physicalDevice.queueFamilyIndices().graphicsFamily());
        presentationQueue = createVkQueue(physicalDevice.queueFamilyIndices().presentationFamily());
    }

    public VkDevice vkDevice() {
        return vkDevice;
    }

    public VkQueue graphicsQueue() {
        return graphicsQueue;
    }

    public VkQueue presentationQueue() {
        return presentationQueue;
    }

    @Override
    public void free() {
        vkDestroyDevice(vkDevice, null);
    }

    private VkDevice createVkDevice(VulkanPhysicalDevice physicalDevice) {

        try(MemoryStack stack = stackPush()) {

            QueueFamilyIndices queueFamilyIndices = physicalDevice.queueFamilyIndices();

            int[] uniqueQueueFamilies = queueFamilyIndices.unique();

            VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.callocStack(uniqueQueueFamilies.length, stack);

            for(int i = 0;i < uniqueQueueFamilies.length;i++) {
                queueCreateInfos.get(i)
                    .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                    .queueFamilyIndex(uniqueQueueFamilies[i])
                    .pQueuePriorities(stack.floats(1.0f));
            }

            VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.callocStack(stack)
                .samplerAnisotropy(true)
                .sampleRateShading(true); // Enable sample shading feature for the device

            VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.callocStack(stack)
                .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                .pQueueCreateInfos(queueCreateInfos)
                .pEnabledFeatures(deviceFeatures)
                .ppEnabledExtensionNames(stringPointers(DEVICE_EXTENSIONS));

            if(VULKAN_DEBUG_MESSAGES_ENABLED) {
                createInfo.ppEnabledLayerNames(stringPointers(VALIDATION_LAYERS));
            }

            PointerBuffer pDevice = stack.pointers(VK_NULL_HANDLE);

            vkCall(vkCreateDevice(physicalDevice.vkPhysicalDevice(), createInfo, null, pDevice));

            return new VkDevice(pDevice.get(0), physicalDevice.vkPhysicalDevice(), createInfo);
        }
    }

    private VkQueue createVkQueue(int queueFamily) {

        try(MemoryStack stack = stackPush()) {

            PointerBuffer pQueue = stack.mallocPointer(1);

            vkGetDeviceQueue(vkDevice, queueFamily, 0, pQueue);

            return new VkQueue(pQueue.get(0), vkDevice);
        }
    }

}
