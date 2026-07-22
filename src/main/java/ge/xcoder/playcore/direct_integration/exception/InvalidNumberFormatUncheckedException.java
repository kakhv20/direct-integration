package ge.xcoder.playcore.direct_integration.exception;

public class InvalidNumberFormatUncheckedException extends BaseUncheckedException {
    public InvalidNumberFormatUncheckedException(String message) {
        super(message);
    }

    public InvalidNumberFormatUncheckedException(String message, Long errorCode) {
        super(message, errorCode);
    }

    public InvalidNumberFormatUncheckedException(String message, Long errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
