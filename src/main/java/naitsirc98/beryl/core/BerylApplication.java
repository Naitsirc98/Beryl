package naitsirc98.beryl.core;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.types.Singleton;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class BerylApplication {

    // Application instance used by Beryl
    @Singleton
    private static BerylApplication instance;

    public static void exit() {
        instance.running = false;
    }

    private boolean running;

    public BerylApplication() {
    }

    public final boolean running() {
        return running;
    }

    protected void onInit() {

    }

    protected void onStart() {

    }

    protected void onUpdate() {

    }

    protected void onRender() {

    }

    protected void onError(Throwable error) {
        Log.error("An unexpected error has crashed the Application" + errorMessage(error), error);
    }

    private String errorMessage(Throwable error) {
        String message = error.getMessage();
        return message == null ? "" : message;
    }

    protected void onTerminate() {

    }

    void start() {
        running = true;
        onStart();
    }

    public static final class Builder {

        private Runnable setConfig = () -> {};
        private Runnable onInit = () -> {};
        private Runnable onStart = () -> {};
        private Runnable onUpdate = () -> {};
        private Runnable onRender = () -> {};
        private Consumer<Throwable> onError = e -> {};
        private Runnable onTerminate = () -> {};

        public Builder() {
        }

        public Builder setConfiguration(Runnable setConfig) {
            this.setConfig = requireNonNull(setConfig);
            return this;
        }

        public Builder onInit(Runnable onInit) {
            this.onInit = requireNonNull(onInit);
            return this;
        }

        public Builder onStart(Runnable onStart) {
            this.onStart = requireNonNull(onStart);
            return this;
        }

        public Builder onUpdate(Runnable onUpdate) {
            this.onUpdate = requireNonNull(onUpdate);
            return this;
        }

        public Builder onRender(Runnable onRender) {
            this.onRender = requireNonNull(onRender);
            return this;
        }

        public Builder onError(Consumer<Throwable> onError) {
            this.onError = requireNonNull(onError);
            return this;
        }

        public Builder onTerminate(Runnable onTerminate) {
            this.onTerminate = requireNonNull(onTerminate);
            return this;
        }

        public BerylApplication build() {
            return new BerylApplication() {

                @Override
                protected void onInit() {
                    onInit.run();
                }

                @Override
                protected void onStart() {
                    onStart.run();
                }

                @Override
                protected void onUpdate() {
                    onUpdate.run();
                }

                @Override
                protected void onRender() {
                    onRender.run();
                }

                @Override
                protected void onError(Throwable error) {
                    onError.accept(error);
                }

                @Override
                protected void onTerminate() {
                    onTerminate.run();
                }
            };
        }
    }

}
