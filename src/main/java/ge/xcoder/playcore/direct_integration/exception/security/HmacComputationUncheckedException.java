package ge.xcoder.playcore.direct_integration.exception.security;

public class HmacComputationUncheckedException extends RuntimeException {
    public HmacComputationUncheckedException(String message, Throwable e) {
        super(message, e);
    }
}
