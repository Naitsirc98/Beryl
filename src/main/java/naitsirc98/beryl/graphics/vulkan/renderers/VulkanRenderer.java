package naitsirc98.beryl.graphics.vulkan.renderers;

import naitsirc98.beryl.graphics.rendering.Renderer;
import naitsirc98.beryl.graphics.vulkan.commands.VulkanCommandPool;
import naitsirc98.beryl.graphics.vulkan.swapchain.FrameManager;
import naitsirc98.beryl.graphics.vulkan.swapchain.VulkanSwapchain;
import naitsirc98.beryl.logging.Log;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.getVulkanErrorName;
import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static naitsirc98.beryl.logging.Log.Level.FATAL;
import static naitsirc98.beryl.util.DataType.UINT64_MAX;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.vkWaitForFences;

public class VulkanRenderer implements Renderer {

    private final VulkanSwapchain swapchain;
    private final VkDevice logicalDevice;
    private final FrameManager frameManager;
    private final VkQueue graphicsQueue;
    private final VkQueue presentationQueue;
    private final VulkanCommandPool commandPool;
    private final VkCommandBuffer[] commandBuffers;
    private boolean framebufferResize;
    private int currentSwapchainImageIndex;

    private VulkanRenderer(VulkanSwapchain swapchain, VulkanCommandPool commandPool) {
        this.swapchain = swapchain;
        this.logicalDevice = swapchain.device().logicalDevice().vkDevice();
        graphicsQueue = swapchain.device().logicalDevice().graphicsQueue();
        presentationQueue = swapchain.depthImage().logicalDevice().presentationQueue();
        this.commandPool = commandPool;
        final int swapchainImagesCount = swapchain.swapChainImages().length;
        commandBuffers = commandPool.newPrimaryCommandBuffers(swapchainImagesCount);
        this.frameManager = new FrameManager(logicalDevice, swapchainImagesCount);
    }

    @Override
    public void begin(MemoryStack stack) {

        FrameManager.Frame frame = frameManager.currentFrame();

        vkWaitForFences(logicalDevice, stack.longs(frame.fence), true, UINT64_MAX);

        IntBuffer pImageIndex = stack.mallocInt(1);

        int vkResult = vkAcquireNextImageKHR(logicalDevice, swapchain.vkSwapchain(), UINT64_MAX,
                frame.imageAvailableSemaphore, VK_NULL_HANDLE, pImageIndex);

        if(vkResult == VK_ERROR_OUT_OF_DATE_KHR) {
            // TODO
            // recreateSwapChain();
            return;
        } else if(vkResult != VK_SUCCESS) {
            Log.fatal("Cannot acquire swapchain image: " + getVulkanErrorName(vkResult));
            return;
        }

        final int imageIndex = currentSwapchainImageIndex = pImageIndex.get(0);

        if(frameManager.isInFlight(imageIndex)) {
            vkWaitForFences(logicalDevice, frameManager.getInFlight(imageIndex).fence, true, UINT64_MAX);
        }

        frameManager.setInFlight(imageIndex, frame);
    }

    @Override
    public void end(MemoryStack stack) {

        FrameManager.Frame frame = frameManager.currentFrame();

        VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack);
        submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);

        submitInfo.waitSemaphoreCount(1);
        submitInfo.pWaitSemaphores(stack.longs(frame.imageAvailableSemaphore));
        submitInfo.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));

        submitInfo.pSignalSemaphores(stack.longs(frame.renderFinishedSemaphore));

        submitInfo.pCommandBuffers(stack.pointers(currentCommandBuffer()));

        vkResetFences(logicalDevice, frame.fence);

        if(!vkCall(vkQueueSubmit(graphicsQueue, submitInfo, frame.fence), FATAL)) {
            vkResetFences(logicalDevice, frame.fence);
            return;
        }

        VkPresentInfoKHR presentInfo = VkPresentInfoKHR.callocStack(stack);
        presentInfo.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);

        presentInfo.pWaitSemaphores(stack.longs(frame.renderFinishedSemaphore));

        presentInfo.swapchainCount(1);
        presentInfo.pSwapchains(stack.longs(swapchain.vkSwapchain()));

        presentInfo.pImageIndices(stack.ints(currentSwapchainImageIndex));

        final int presentResult = vkQueuePresentKHR(presentationQueue, presentInfo);

        if(presentResult == VK_ERROR_OUT_OF_DATE_KHR || presentResult == VK_SUBOPTIMAL_KHR || framebufferResize) {
            framebufferResize = false;
            // TODO
            // recreateSwapChain();
        } else if(presentResult != VK_SUCCESS) {
            Log.fatal("Failed to present swap chain image: " + getVulkanErrorName(presentResult));
        }

        frameManager.endFrame();
    }

    public VkCommandBuffer currentCommandBuffer() {
        return commandBuffers[currentSwapchainImageIndex];
    }

    @Override
    public void free() {
        commandPool.freeCommandBuffers(commandBuffers);
        frameManager.free();
    }
}
