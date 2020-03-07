package naitsirc98.beryl.core;

import naitsirc98.beryl.logging.Log;

import java.io.OutputStream;
import java.io.PrintStream;

public class LWJGLDebugStream extends PrintStream {

    public LWJGLDebugStream() {
        super(new LWJGLOutputStream());
    }

    private static class LWJGLOutputStream extends OutputStream {

        private byte[] buffer;
        private int pointer;

        public LWJGLOutputStream() {
            this.buffer = new byte[1024];
        }

        @Override
        public void write(int b) {

            if(b == '\t' || b == '\n' && pointer == 0) {
                return;
            }

            // Log already appends a \n after the message
            if(b != '\n') {
                buffer[pointer++] = (byte)b;
            }

            if(pointer >= buffer.length || b == '\n') {
                flush();
            }
        }

        @Override
        public void flush() {
            Log.lwjgl(new String(buffer, 0, pointer));
            pointer = 0;
        }

        @Override
        public void close() {
            buffer = null;
        }
    }
}
