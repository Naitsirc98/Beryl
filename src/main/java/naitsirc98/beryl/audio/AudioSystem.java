package naitsirc98.beryl.audio;

import naitsirc98.beryl.core.BerylSystem;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.types.Singleton;
import org.lwjgl.openal.ALC;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.audio.AudioDistanceModel.EXPONENT_DISTANCE;
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.*;
import static org.lwjgl.openal.ALC10.ALC_DEFAULT_DEVICE_SPECIFIER;
import static org.lwjgl.openal.ALC10.alcGetString;

public final class AudioSystem extends BerylSystem {

    @Singleton
    private static AudioSystem instance;

    private static AudioDistanceModel distanceModel;

    public static AudioDistanceModel distanceModel() {
        return distanceModel;
    }

    public static void distanceModel(AudioDistanceModel distanceModel) {
        alDistanceModel(asOpenALDistanceModel(requireNonNull(distanceModel)));
        AudioSystem.distanceModel = distanceModel;
    }

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

        distanceModel(EXPONENT_DISTANCE);

        AudioListener.instance = listener;
        AudioDevice.instance = device;
        AudioContext.instance = context;
    }

    @Override
    protected void terminate() {

        context.release();
        device.release();

        ALC.destroy();

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

    private static int asOpenALDistanceModel(AudioDistanceModel distanceModel) {

        switch(distanceModel) {

            case EXPONENT_DISTANCE:
                return AL_EXPONENT_DISTANCE;
            case EXPONENT_DISTANCE_CLAMPED:
                return AL_EXPONENT_DISTANCE_CLAMPED;
            case INVERSE_DISTANCE:
                return AL_INVERSE_DISTANCE;
            case INVERSE_DISTANCE_CLAMPED:
                return AL_INVERSE_DISTANCE_CLAMPED;
            case LINEAR_DISTANCE:
                return AL_LINEAR_DISTANCE;
            case LINEAR_DISTANCE_CLAMPED:
                return AL_LINEAR_DISTANCE_CLAMPED;
        }

        Log.error("Unknown distance model " + distanceModel());
        return AL_EXPONENT_DISTANCE;
    }
}
