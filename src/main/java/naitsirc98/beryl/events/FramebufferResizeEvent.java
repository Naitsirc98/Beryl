package naitsirc98.beryl.events;

public class FramebufferResizeEvent extends Event {

    private final int width;
    private final int height;

    public FramebufferResizeEvent(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public Class<? extends Event> type() {
        return FramebufferResizeEvent.class;
    }
}
