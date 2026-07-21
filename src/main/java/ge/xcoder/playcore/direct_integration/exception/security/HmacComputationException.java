package ge.xcoder.playcore.direct_integration.exception.security;

import ge.xcoder.playcore.direct_integration.exception.BaseException;

public class HmacComputationException extends BaseException {
    public HmacComputationException(String message, Throwable e) {
        super(message, e);
    }
}
