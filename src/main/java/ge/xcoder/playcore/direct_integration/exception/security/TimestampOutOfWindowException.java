package ge.xcoder.playcore.direct_integration.exception.security;

import ge.xcoder.playcore.direct_integration.exception.BaseException;

public class TimestampOutOfWindowException extends BaseException {
    public TimestampOutOfWindowException(String message, Long errorCode) {
        super(message, errorCode);
    }
}
