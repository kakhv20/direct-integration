package ge.xcoder.playcore.direct_integration.exception.security;

import ge.xcoder.playcore.direct_integration.exception.BaseUncheckedException;

public class TimestampOutOfWindowUncheckedException extends BaseUncheckedException {
    public TimestampOutOfWindowUncheckedException(String message, Long errorCode) {
        super(message, errorCode);
    }
}
