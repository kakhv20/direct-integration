package ge.xcoder.playcore.direct_integration.exception;

public class RepeatedValueUncheckedException extends BaseUncheckedException {
    public RepeatedValueUncheckedException(String message, Long errorCode) {
        super(message, errorCode);
    }
}
