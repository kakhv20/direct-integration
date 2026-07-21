package ge.xcoder.playcore.direct_integration.exception;

public class InvalidNumberFormatException extends BaseException {
    public InvalidNumberFormatException(String message) {
        super(message);
    }

    public InvalidNumberFormatException(String message, Long errorCode) {
        super(message, errorCode);
    }

    public InvalidNumberFormatException(String message, Long errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
