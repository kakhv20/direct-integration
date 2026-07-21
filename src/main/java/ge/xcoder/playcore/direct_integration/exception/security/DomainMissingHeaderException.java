package ge.xcoder.playcore.direct_integration.exception.security;

import ge.xcoder.playcore.direct_integration.exception.BaseException;
import ge.xcoder.playcore.direct_integration.util.ErrorCodes;

public class DomainMissingHeaderException extends BaseException {
    public DomainMissingHeaderException(String message) {
        super(message, ErrorCodes.MISSING_HEADER);
    }
}
