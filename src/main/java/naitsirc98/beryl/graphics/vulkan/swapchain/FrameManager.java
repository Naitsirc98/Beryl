package naitsirc98.beryl.graphics.vulkan.swapchain;

import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.util.types.Destructor;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.Map;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static naitsirc98.beryl.logging.Log.Level.FATAL;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

@Destructor
public class FrameManager implements NativeResource {

    private Frame[] frames;
    private Map<Integer, Frame> inFlight;
    private int currentFrame;

    public FrameManager(int size) {
        frames = createFrames(size);
        inFlight = new HashMap<>();
    }

    public Frame currentFrame() {
        return frames[currentFrame];
    }

    public void endFrame() {
        currentFrame = (currentFrame + 1) % maxFramesInFlight();
    }

    public boolean isInFlight(int index) {
        return inFlight.containsKey(index);
    }

    public Frame getInFlight(int index) {
        return inFlight.get(index);
    }

    public void setInFlight(int index, Frame frame) {
        inFlight.put(index, frame);
    }

    public int maxFramesInFlight() {
        return frames.length;
    }

    private Frame[] createFrames(int size) {

        frames = new Frame[size];

        try(MemoryStack stack = stackPush()) {

            VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.callocStack(stack);
            semaphoreInfo.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

            VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.callocStack(stack);
            fenceInfo.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
            fenceInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT);

            LongBuffer pImageAvailableSemaphore = stack.mallocLong(1);
            LongBuffer pRenderFinishedSemaphore = stack.mallocLong(1);
            LongBuffer pFence = stack.mallocLong(1);

            VkDevice device = Graphics.vulkan().logicalDevice().handle();

            for(int i = 0;i < size;i++) {

                vkCall(vkCreateSemaphore(device, semaphoreInfo, null, pImageAvailableSemaphore), FATAL);
                vkCall(vkCreateSemaphore(device, semaphoreInfo, null, pRenderFinishedSemaphore), FATAL);
                vkCall(vkCreateFence(device, fenceInfo, null, pFence), FATAL);

                frames[i] = new Frame(pImageAvailableSemaphore.get(0), pRenderFinishedSemaphore.get(0), pFence.get(0));
            }

        }

        return frames;
    }

    @Override
    public void free() {
        for(int i = 0;i < frames.length;i++) {
            frames[i].free();
            frames[i] = null;
        }
    }

    /**
     * Wraps the needed sync objects for an in flight frame
     *
     * This frame's sync objects must be deleted manually
     * */
    @Destructor
    public class Frame implements NativeResource {

        public final long imageAvailableSemaphore;
        public final long renderFinishedSemaphore;
        public final long fence;

        private Frame(long imageAvailableSemaphore, long renderFinishedSemaphore, long fence) {
            this.imageAvailableSemaphore = imageAvailableSemaphore;
            this.renderFinishedSemaphore = renderFinishedSemaphore;
            this.fence = fence;
        }

        @Override
        public void free() {
            VkDevice device = Graphics.vulkan().logicalDevice().handle();
            vkDestroySemaphore(device, imageAvailableSemaphore, null);
            vkDestroySemaphore(device, renderFinishedSemaphore, null);
            vkDestroyFence(device, fence, null);
        }
    }
}
