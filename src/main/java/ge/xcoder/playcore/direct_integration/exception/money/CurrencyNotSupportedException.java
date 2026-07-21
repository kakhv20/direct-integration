package ge.xcoder.playcore.direct_integration.exception.money;

import ge.xcoder.playcore.direct_integration.exception.BaseException;
import ge.xcoder.playcore.direct_integration.util.ErrorCodes;

public class CurrencyNotSupportedException extends BaseException {
    public CurrencyNotSupportedException(String message) {
        super(message, ErrorCodes.CURRENCY_NOT_SUPPORTED);
    }
}
