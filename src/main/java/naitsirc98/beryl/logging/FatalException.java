package naitsirc98.beryl.logging;

public class FatalException extends RuntimeException {

    public FatalException(String msg) {
        super(msg);
    }

    public FatalException(Throwable cause) {
        super(cause);
    }

    public FatalException(String message, Throwable cause) {
        super(message, cause);
    }
}
