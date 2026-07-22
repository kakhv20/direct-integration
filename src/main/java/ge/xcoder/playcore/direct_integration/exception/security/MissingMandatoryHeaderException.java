package ge.xcoder.playcore.direct_integration.exception.security;

import ge.xcoder.playcore.direct_integration.exception.BaseException;
import ge.xcoder.playcore.direct_integration.util.ErrorCodes;

public class MissingMandatoryHeaderException extends BaseException {
    public MissingMandatoryHeaderException(String message) {
        super(message, ErrorCodes.MISSING_HEADER);
    }
}
