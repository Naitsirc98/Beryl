package naitsirc98.beryl.audio;

import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.util.types.Singleton;

import static naitsirc98.beryl.util.types.TypeUtils.initSingleton;
import static org.lwjgl.openal.ALC10.ALC_DEFAULT_DEVICE_SPECIFIER;
import static org.lwjgl.openal.ALC10.alcGetString;

public final class AudioSystem extends BerylSystem {

    @Singleton
    private static AudioSystem instance;

    private AudioContext context;
    private AudioDevice device;
    private AudioListener listener;

    private AudioSystem() {

    }

    @Override
    protected void init() {

        device = createDefaultDevice();
        context = new AudioContext(device);
        listener = new AudioListener();

        AudioListener.instance = listener;
        AudioDevice.instance = device;
        AudioContext.instance = context;
    }

    @Override
    protected void terminate() {

        context.release();
        device.release();

        AudioListener.instance = null;
        AudioDevice.instance = null;
        AudioContext.instance = null;
    }

    public void recreate() {
        init();
    }

    private AudioDevice createDefaultDevice() {
        return new AudioDevice(alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER));
    }

    public void update() {
        listener.update();
    }
}
