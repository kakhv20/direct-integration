package ge.xcoder.playcore.direct_integration.exception;

public class InvalidNumberFormatException extends BaseException {
    public InvalidNumberFormatException(String message, long errorCode) {
        super(message, errorCode);
    }

    public InvalidNumberFormatException(String message, long errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
