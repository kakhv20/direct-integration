package ge.xcoder.playcore.direct_integration.exception;

public class RepeatedValueException extends BaseException {
    public RepeatedValueException(String message, Long errorCode) {
        super(message, errorCode);
    }
}
