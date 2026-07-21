package ge.xcoder.playcore.direct_integration.exception;

public class RepeatedValueException extends BaseException {
    public RepeatedValueException(String message, long errorCode) {
        super(message, errorCode);
    }
}
