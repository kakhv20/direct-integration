package ge.xcoder.playcore.direct_integration.exception.security;

import ge.xcoder.playcore.direct_integration.exception.BaseUncheckedException;
import ge.xcoder.playcore.direct_integration.util.ErrorCodes;

public class WrongOperatorUncheckedException extends BaseUncheckedException {
    public WrongOperatorUncheckedException(String message) {
        super(message, ErrorCodes.WRONG_OPERATOR);
    }
}
