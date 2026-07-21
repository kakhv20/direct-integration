package ge.xcoder.playcore.direct_integration.exception.security;

import ge.xcoder.playcore.direct_integration.exception.BaseException;
import ge.xcoder.playcore.direct_integration.util.ErrorCodes;

public class InvalidSignatureException extends BaseException {
    public InvalidSignatureException(String message) {
        super(message, ErrorCodes.INVALID_SIGNATURE);
    }
}
