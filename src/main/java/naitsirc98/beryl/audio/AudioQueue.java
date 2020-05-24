package naitsirc98.beryl.audio;

import org.lwjgl.system.MemoryStack;

import static naitsirc98.beryl.audio.AudioDebug.checkAudioErrors;
import static naitsirc98.beryl.core.BerylConfigConstants.DEBUG;
import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static naitsirc98.beryl.util.handles.IntHandle.NULL;
import static org.lwjgl.openal.AL10.*;

public class AudioQueue {

    private final int sourceHandle;

    AudioQueue(int sourceHandle) {
        this.sourceHandle = sourceHandle;
    }

    public void enqueue(AudioBuffer buffer) {
        assertNonNull(buffer);
        alSourceQueueBuffers(sourceHandle, buffer.handle());
        if(DEBUG) {
            checkAudioErrors();
        }
    }

    public boolean empty() {
        return size() == 0;
    }

    public int size() {
        return alGetSourcei(sourceHandle, AL_BUFFERS_QUEUED);
    }

    public int processed() {
        return alGetSourcei(sourceHandle, AL_BUFFERS_PROCESSED);
    }

    public int pending() {
        return size() - processed();
    }

    public int unqueue() {
        final int count = processed();
        if(count > 0) {
            alSourceUnqueueBuffers(sourceHandle);
        }
        return count;
    }

    public void unqueue(int count) {
        assertTrue(count >= 0);
        if(count == 0) {
            return;
        }
        try(MemoryStack stack = MemoryStack.stackPush()) {
            alSourceUnqueueBuffers(sourceHandle, stack.mallocInt(count));
        }
    }

    public void clear() {
        alSourcei(sourceHandle, AL_BUFFER, NULL);
    }

}
