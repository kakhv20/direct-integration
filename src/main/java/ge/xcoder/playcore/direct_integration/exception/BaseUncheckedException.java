package ge.xcoder.playcore.direct_integration.exception;

import lombok.Getter;

public class BaseUncheckedException extends RuntimeException {
    @Getter
    protected final Long errorCode;

    public BaseUncheckedException(String message) {
        super(message);
        this.errorCode = null;
    }

    public BaseUncheckedException(String message, Long errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BaseUncheckedException(String message, Throwable e) {
        super(message, e);
        this.errorCode = null;
    }

    public BaseUncheckedException(String message, Long errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
