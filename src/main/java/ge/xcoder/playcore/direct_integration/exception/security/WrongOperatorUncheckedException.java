package ge.xcoder.playcore.direct_integration.exception.security;

import ge.xcoder.playcore.direct_integration.exception.BaseUncheckedException;
import ge.xcoder.playcore.direct_integration.util.ResultCodes;

public class WrongOperatorUncheckedException extends BaseUncheckedException {
    public WrongOperatorUncheckedException(String message) {
        super(message, ResultCodes.WRONG_OPERATOR);
    }
}
