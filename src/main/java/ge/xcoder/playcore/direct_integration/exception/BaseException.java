package ge.xcoder.playcore.direct_integration.exception;

import lombok.Getter;

public class BaseException extends RuntimeException {
    @Getter
    protected final Long errorCode;

    public BaseException(String message) {
        super(message);
        this.errorCode = null;
    }

    public BaseException(String message, Long errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BaseException(String message, Throwable e) {
        super(message, e);
        this.errorCode = null;
    }

    public BaseException(String message, Long errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
