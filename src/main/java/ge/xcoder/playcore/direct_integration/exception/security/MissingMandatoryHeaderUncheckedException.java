package ge.xcoder.playcore.direct_integration.exception.security;

import ge.xcoder.playcore.direct_integration.exception.BaseUncheckedException;
import ge.xcoder.playcore.direct_integration.util.ErrorCodes;

public class MissingMandatoryHeaderUncheckedException extends BaseUncheckedException {
    public MissingMandatoryHeaderUncheckedException(String message) {
        super(message, ErrorCodes.MISSING_HEADER);
    }
}
