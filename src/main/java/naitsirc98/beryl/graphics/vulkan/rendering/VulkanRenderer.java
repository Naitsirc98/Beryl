package naitsirc98.beryl.graphics.vulkan.rendering;

import naitsirc98.beryl.graphics.rendering.Renderer;
import naitsirc98.beryl.graphics.vulkan.VulkanObject;
import naitsirc98.beryl.graphics.vulkan.commands.VulkanCommandPool;
import naitsirc98.beryl.graphics.vulkan.devices.VulkanLogicalDevice;
import naitsirc98.beryl.graphics.vulkan.swapchain.FrameManager;
import naitsirc98.beryl.graphics.vulkan.swapchain.VulkanSwapchain;
import naitsirc98.beryl.graphics.vulkan.swapchain.VulkanSwapchainDependent;
import naitsirc98.beryl.logging.Log;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;

import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.getVulkanErrorName;
import static naitsirc98.beryl.graphics.vulkan.util.VulkanUtils.vkCall;
import static naitsirc98.beryl.logging.Log.Level.FATAL;
import static naitsirc98.beryl.util.types.DataType.UINT64_MAX;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanRenderer implements VulkanObject, Renderer, VulkanSwapchainDependent {

    public static VulkanRenderer get() {
        return Renderer.get();
    }

    private VulkanSwapchain swapchain;
    private VkDevice logicalDevice;
    private FrameManager frameManager;
    private VkQueue graphicsQueue;
    private VkQueue presentationQueue;
    private VulkanCommandPool commandPool;
    private VkCommandBuffer[] commandBuffers;
    private int currentSwapchainImageIndex;

    private VulkanRenderer() {
        init();
    }

    @Override
    public boolean begin() {

        try(MemoryStack stack = stackPush()) {

            FrameManager.Frame frame = frameManager.currentFrame();

            vkWaitForFences(logicalDevice, stack.longs(frame.fence), true, UINT64_MAX);

            IntBuffer pImageIndex = stack.mallocInt(1);

            int vkResult = vkAcquireNextImageKHR(logicalDevice, swapchain.handle(), UINT64_MAX,
                    frame.imageAvailableSemaphore, VK_NULL_HANDLE, pImageIndex);

            if(vkResult == VK_ERROR_OUT_OF_DATE_KHR) {
                swapchain.recreate();
                return false;
            } else if(vkResult != VK_SUCCESS) {
                Log.fatal("Cannot acquire swapchain image: " + getVulkanErrorName(vkResult));
                return false;
            }

            final int imageIndex = currentSwapchainImageIndex = pImageIndex.get(0);

            if(frameManager.isInFlight(imageIndex)) {
                vkWaitForFences(logicalDevice, frameManager.getInFlight(imageIndex).fence, true, UINT64_MAX);
            }

            frameManager.setInFlight(imageIndex, frame);
        }

        return true;
    }

    @Override
    public void end() {

        try(MemoryStack stack = stackPush()) {

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

            if(swapchain.vsync()) {
                vkQueueWaitIdle(graphicsQueue);
            }

            VkPresentInfoKHR presentInfo = VkPresentInfoKHR.callocStack(stack);
            presentInfo.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);

            presentInfo.pWaitSemaphores(stack.longs(frame.renderFinishedSemaphore));

            presentInfo.swapchainCount(1);
            presentInfo.pSwapchains(stack.longs(swapchain.handle()));

            presentInfo.pImageIndices(stack.ints(currentSwapchainImageIndex));

            final int presentResult = vkQueuePresentKHR(presentationQueue, presentInfo);

            if(presentResult == VK_ERROR_OUT_OF_DATE_KHR || presentResult == VK_SUBOPTIMAL_KHR) {
                swapchain.recreate();
            } else if(presentResult != VK_SUCCESS) {
                Log.fatal("Failed to present swap chain image: " + getVulkanErrorName(presentResult));
            }

            frameManager.endFrame();
        }
    }

    public int currentSwapchainImageIndex() {
        return currentSwapchainImageIndex;
    }

    public VkCommandBuffer currentCommandBuffer() {
        return commandBuffers[currentSwapchainImageIndex];
    }

    @Override
    public void release() {
        logicalDevice().waitIdle();
        commandPool.freeCommandBuffers(commandBuffers);
        frameManager.release();
    }

    @Override
    public void onSwapchainRecreate() {
        logicalDevice().waitIdle();
        commandPool.freeCommandBuffers(commandBuffers);
        init0();
    }

    private void init() {
        init0();
        this.frameManager = new FrameManager(swapchain.imageCount());
        currentSwapchainImageIndex = 0;
        swapchain.addSwapchainDependent(this);
    }

    private void init0() {
        VulkanLogicalDevice logicalDevice = logicalDevice();
        this.swapchain = swapchain();
        this.logicalDevice = logicalDevice.handle();
        graphicsQueue = logicalDevice.graphicsQueue();
        presentationQueue = logicalDevice.presentationQueue();
        this.commandPool = graphicsCommandPool();
        commandBuffers = commandPool.newPrimaryCommandBuffers(swapchain.imageCount());
    }
}
