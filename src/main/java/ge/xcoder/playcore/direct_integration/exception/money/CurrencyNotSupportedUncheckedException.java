package ge.xcoder.playcore.direct_integration.exception.money;

import ge.xcoder.playcore.direct_integration.exception.BaseUncheckedException;
import ge.xcoder.playcore.direct_integration.util.ErrorCodes;

public class CurrencyNotSupportedUncheckedException extends BaseUncheckedException {
    public CurrencyNotSupportedUncheckedException(String message) {
        super(message, ErrorCodes.CURRENCY_NOT_SUPPORTED);
    }
}
