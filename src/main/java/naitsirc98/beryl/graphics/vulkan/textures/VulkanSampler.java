package naitsirc98.beryl.graphics.vulkan.textures;

import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

import java.nio.LongBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanSampler implements VulkanObject.Long {

    private long handle;
    private VkSamplerCreateInfo info;

    public VulkanSampler(VkSamplerCreateInfo info) {
        init(info);
    }

    @Override
    public long handle() {
        return handle;
    }

    public int minFilter() {
        return info.minFilter();
    }

    public int magFilter() {
        return info.magFilter();
    }

    public int mipmapMode() {
        return info.mipmapMode();
    }

    public int addressModeU() {
        return info.addressModeU();
    }

    public int addressModeV() {
        return info.addressModeV();
    }

    public int addressModeW() {
        return info.addressModeW();
    }

    public float mipLodBias() {
        return info.mipLodBias();
    }

    public boolean anisotropyEnable() {
        return info.anisotropyEnable();
    }

    public float maxAnisotropy() {
        return info.maxAnisotropy();
    }

    public boolean compareEnable() {
        return info.compareEnable();
    }

    public int compareOp() {
        return info.compareOp();
    }

    public float minLod() {
        return info.minLod();
    }

    public float maxLod() {
        return info.maxLod();
    }

    public int borderColor() {
        return info.borderColor();
    }

    public boolean unnormalizedCoordinates() {
        return info.unnormalizedCoordinates();
    }

    public int flags() {
        return info.flags();
    }

    @Override
    public void free() {

        vkDestroySampler(logicalDevice().handle(), handle, null);
        info.free();

        handle = VK_NULL_HANDLE;
        info = null;
    }

    private void init(VkSamplerCreateInfo createInfo) {

        try(MemoryStack stack = stackPush()) {

            this.info = createInfo;

            LongBuffer pSampler = stack.mallocLong(1);

            vkCall(vkCreateSampler(logicalDevice().handle(), createInfo, null, pSampler));

            handle = pSampler.get(0);
        }
    }
}
