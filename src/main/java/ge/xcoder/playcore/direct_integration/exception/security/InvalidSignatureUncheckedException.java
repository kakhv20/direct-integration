package ge.xcoder.playcore.direct_integration.exception.security;

import ge.xcoder.playcore.direct_integration.exception.BaseUncheckedException;
import ge.xcoder.playcore.direct_integration.util.ErrorCodes;

public class InvalidSignatureUncheckedException extends BaseUncheckedException {
    public InvalidSignatureUncheckedException(String message) {
        super(message, ErrorCodes.INVALID_SIGNATURE);
    }
}
