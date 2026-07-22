package ge.xcoder.playcore.direct_integration.exception.security;

import ge.xcoder.playcore.direct_integration.exception.BaseException;
import ge.xcoder.playcore.direct_integration.util.ErrorCodes;

public class WrongOperatorException extends BaseException {
    public WrongOperatorException(String message) {
        super(message, ErrorCodes.WRONG_OPERATOR);
    }
}
