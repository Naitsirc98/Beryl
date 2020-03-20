package naitsirc98.beryl.graphics.vulkan.textures;

import naitsirc98.beryl.graphics.textures.Sampler;
import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanSampler implements VulkanObject.Long, Sampler {

    private long handle;
    private VkSamplerCreateInfo info;
    private boolean modified = true;

    VulkanSampler() {
        init(createDefaultSamplerCreateInfo());
    }

    VulkanSampler(VkSamplerCreateInfo info) {
        init(info);
    }

    @Override
    public long handle() {
        return handle;
    }

    public void validate() {
        if(modified) {
            vkDestroySampler(logicalDevice().handle(), handle, null);
            init(info);
            modified = false;
        }
    }

    @Override
    public WrapMode wrapModeS() {
        return mapper().mapFromAPI(WrapMode.class, info.addressModeU());
    }

    @Override
    public Sampler wrapModeS(WrapMode wrapMode) {
        info.addressModeU(mapper().mapToAPI(wrapMode));
        modified = true;
        return this;
    }

    @Override
    public WrapMode wrapModeT() {
        return mapper().mapFromAPI(WrapMode.class, info.addressModeV());
    }

    @Override
    public Sampler wrapModeT(WrapMode wrapMode) {
        info.addressModeV(mapper().mapToAPI(wrapMode));
        modified = true;
        return this;
    }

    @Override
    public WrapMode wrapModeR() {
        return mapper().mapFromAPI(WrapMode.class, info.addressModeW());
    }

    @Override
    public Sampler wrapModeR(WrapMode wrapMode) {
        info.addressModeW(mapper().mapToAPI(wrapMode));
        modified = true;
        return this;
    }

    @Override
    public MinFilter minFilter() {
        return mapper().mapMinFilterFromAPI(info.minFilter(), info.mipmapMode());
    }

    @Override
    public Sampler minFilter(MinFilter minFilter) {
        try(MemoryStack stack = stackPush()) {
            IntBuffer vkMinFilterValues = mapper().mapMinFilterToAPI(minFilter, stack.mallocInt(2));
            info.minFilter(vkMinFilterValues.get(0)).mipmapMode(vkMinFilterValues.get(1));
            modified = true;
            return this;
        }
    }

    @Override
    public MagFilter magFilter() {
        return mapper().mapFromAPI(MagFilter.class, info.magFilter());
    }

    @Override
    public Sampler magFilter(MagFilter magFilter) {
        info.magFilter(mapper().mapToAPI(magFilter));
        modified = true;
        return this;
    }

    @Override
    public float maxSupportedAnisotropy() {
        return physicalDevice().properties().limits().maxSamplerAnisotropy();
    }

    @Override
    public float maxAnisotropy() {
        return info.maxAnisotropy();
    }

    @Override
    public Sampler maxAnisotropy(float maxAnisotropy) {
        info.maxAnisotropy(maxAnisotropy);
        modified = true;
        return this;
    }

    @Override
    public boolean compareEnable() {
        return info.compareEnable();
    }

    @Override
    public Sampler compareEnable(boolean enable) {
        info.compareEnable(enable);
        modified = true;
        return this;
    }

    @Override
    public CompareOperation compareOperation() {
        return mapper().mapFromAPI(CompareOperation.class, info.compareOp());
    }

    @Override
    public Sampler compareOperation(CompareOperation compareOperation) {
        info.compareOp(mapper().mapToAPI(compareOperation));
        modified = true;
        return this;
    }

    @Override
    public float minLod() {
        return info.minLod();
    }

    @Override
    public Sampler minLod(float minLod) {
        info.minLod(minLod);
        modified = true;
        return this;
    }

    @Override
    public float maxLod() {
        return info.maxLod();
    }

    @Override
    public Sampler maxLod(float maxLod) {
        info.maxLod(maxLod);
        modified = true;
        return this;
    }

    @Override
    public float lodBias() {
        return info.mipLodBias();
    }

    @Override
    public Sampler lodBias(float lodBias) {
        info.mipLodBias(lodBias);
        modified = true;
        return this;
    }

    @Override
    public BorderColor borderColor() {
        return mapper().mapFromAPI(BorderColor.class, info.borderColor());
    }

    @Override
    public Sampler borderColor(BorderColor borderColor) {
        info.borderColor(mapper().mapToAPI(borderColor));
        modified = true;
        return this;
    }

    @Override
    public void release() {

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

    private VkSamplerCreateInfo createDefaultSamplerCreateInfo() {
        return VkSamplerCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO)
                .magFilter(VK_FILTER_LINEAR)
                .minFilter(VK_FILTER_LINEAR)
                .addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT)
                .addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT)
                .addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT)
                .anisotropyEnable(true)
                .maxAnisotropy(16.0f)
                .borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK)
                .unnormalizedCoordinates(false)
                .compareEnable(false)
                .compareOp(VK_COMPARE_OP_ALWAYS)
                .mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR)
                .minLod(0)
                .maxLod(1.0f)
                .mipLodBias(0);
    }
}
