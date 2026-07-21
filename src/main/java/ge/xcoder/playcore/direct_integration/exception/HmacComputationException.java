package ge.xcoder.playcore.direct_integration.exception;

public class HmacComputationException extends RuntimeException {
    public HmacComputationException(String message, Throwable e) {
        super(message, e);
    }
}
